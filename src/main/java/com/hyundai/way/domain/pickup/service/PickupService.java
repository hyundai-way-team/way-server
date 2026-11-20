package com.hyundai.way.domain.pickup.service;

import com.hyundai.way.domain.pickup.domain.Completion;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.domain.vehicle.exception.VehicleErrorCode;
import com.hyundai.way.domain.vehicle.exception.VehicleException;
import com.hyundai.way.domain.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Passenger passenger = command.passenger();
        PickupLocation pickupLocation = command.pickupLocation();
        Locaion locaion = Locaion.create(pickupLocation.address(), pickupLocation.latitude(), pickupLocation.longitude());

        Pickup pickup = Pickup.create(command.requestId(), passenger.name(), locaion, command.destination(), vehicle);
        Completion.create(pickup);
        Pickup savedPickup = pickupRepository.save(pickup);

        //TODO: invitationWebURL 생성 로직 제작
        String invitationWebURL = "https://{webHost}/p/join#code={participationCode}";

        log.info("Save Pickup={}", savedPickup);
        return PickupCreateResult.from(savedPickup.getId(), invitationWebURL);
    }
}
