package com.hyundai.way.domain.pickup.service;

import com.hyundai.way.domain.pickup.domain.Completion;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.exception.PickupException;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.domain.vehicle.exception.VehicleErrorCode;
import com.hyundai.way.domain.vehicle.exception.VehicleException;
import com.hyundai.way.domain.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PickupService {

    private final PickupRepository pickupRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public PickupCreateResult createPickup(PickupCreateCommand command, String vin) {
        Vehicle vehicle = vehicleRepository.findByVehicleKey(vin)
                .filter(registeredVehicle -> vin.equals(registeredVehicle.getVehicleKey()))
                .orElseThrow(() -> new VehicleException(VehicleErrorCode.VEHICLE_NOT_REGISTERED));

        PickupCreateResult existingPickup = findExistingPickup(command);
        if (existingPickup != null) return existingPickup;

        Passenger passenger = command.passenger();
        PickupLocation pickupLocation = command.pickupLocation();
        Locaion locaion = Locaion.create(pickupLocation.address(), pickupLocation.latitude(), pickupLocation.longitude());

        Pickup pickup = Pickup.create(command.requestId(), passenger.name(), locaion, command.destination(), vehicle);
        Completion.create(pickup);
        Pickup savedPickup = pickupRepository.save(pickup);

        return getPickupCreateResult(savedPickup);
    }

    private PickupCreateResult findExistingPickup(PickupCreateCommand command) {
        Optional<Pickup> findExistingPickup = pickupRepository.findByRequestId(command.requestId()).stream()
                .filter(pickup -> pickup.equals(command.requestId()))
                .findFirst();

        return findExistingPickup
                .map(this::getPickupCreateResult)
                .orElse(null);
    }

    private PickupCreateResult getPickupCreateResult(Pickup savedPickup) {
        //TODO: invitationWebURL 생성 로직 제작
        String invitationWebURL = "https://{webHost}/p/join#code={participationCode}";
        return PickupCreateResult.from(savedPickup.getId(), invitationWebURL);
    }
}
