package com.hyundai.way.domain.pickup.service;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.pickup.domain.Completion;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.pickup.secure.ParticipationCode;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.domain.vehicle.exception.VehicleErrorCode;
import com.hyundai.way.domain.vehicle.exception.VehicleException;
import com.hyundai.way.domain.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PickupService {

    private static final SecureRandom INVITATION_CODE = new SecureRandom();

    private final PickupRepository pickupRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public PickupCreateResult createPickup(PickupCreateCommand command, String vin) {
        Vehicle vehicle = findRegistryVehicleFromVin(vin);
        String codeLookupKey = generateParticipationCode();
        PickupCreateResult existingPickup = getExistingPickup(command, vehicle);

        if (existingPickup != null) return existingPickup;

        Passenger passenger = command.passenger();
        PickupLocation pickupLocation = command.pickupLocation();
        Locaion locaion = Locaion.create(pickupLocation.address(), pickupLocation.latitude(), pickupLocation.longitude());

        Pickup pickup = createPickupAggregate(command, passenger, locaion, vehicle, codeLookupKey);

        Pickup savedPickup = pickupRepository.save(pickup);

        //TODO: 추후 웹소켓 기능 제작되면 webHost 전달
        return getPickupCreateResult(savedPickup, "webHost", codeLookupKey);
    }

    private static Pickup createPickupAggregate(PickupCreateCommand command, Passenger passenger, Locaion locaion, Vehicle vehicle, String codeLookupKey) {
        Pickup pickup = Pickup.create(command.requestId(), passenger.name(), locaion, command.destination(), vehicle);
        Invitation.create(pickup, codeLookupKey, Instant.now().plus(Duration.ofMinutes(30)));
        Completion.create(pickup);
        return pickup;
    }

    private PickupCreateResult getExistingPickup(PickupCreateCommand command, Vehicle vehicle) {
        Optional<Pickup> existingPickup = pickupRepository.findByVehicleAndRequestId(vehicle, command.requestId());
        return existingPickup
                .map(pickup -> getPickupCreateResult(pickup, "webHost", ""))
                .orElse(null);
    }

    private Vehicle findRegistryVehicleFromVin(String vin) {
        return vehicleRepository.findByVehicleKey(vin)
                .filter(registeredVehicle -> vin.equals(registeredVehicle.getVehicleKey()))
                .orElseThrow(() -> new VehicleException(VehicleErrorCode.VEHICLE_NOT_REGISTERED));
    }

    private String generateParticipationCode() {
        String code = ParticipationCode.generate(INVITATION_CODE);
        return ParticipationCode.codeLoockupKey(code);
    }

    private PickupCreateResult getPickupCreateResult(
            Pickup savedPickup, String webHost, String participationCode
    ) {
        String invitationWebURL = "https://%s/p/join#code=%s"
                .formatted(webHost, participationCode);

        return PickupCreateResult.from(savedPickup.getId(), invitationWebURL);
    }
}
