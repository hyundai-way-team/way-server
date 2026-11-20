package com.hyundai.way.domain.pickup.service;

import com.hyundai.way.domain.pickup.domain.Completion;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
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

    @Transactional
    public PickupCreateResult createPickup(PickupCreateCommand command, String vehicleKey) {
        Passenger passenger = command.passenger();
        PickupLocation pickupLocation = command.pickupLocation();
        Locaion locaion = Locaion.create(pickupLocation.address(), pickupLocation.latitude(), pickupLocation.longitude());

        Pickup pickup = getPickup(command, vehicleKey, passenger, locaion);
        Pickup savedPickup = pickupRepository.save(pickup);

        //TODO: invitationWebURL 생성 로직 제작
        String invitationWebURL = "https://{webHost}/p/join#code={participationCode}";

        log.info("Save Pickup={}", savedPickup);
        return PickupCreateResult.from(savedPickup.getId(), invitationWebURL);
    }

    private Pickup getPickup(PickupCreateCommand command, String vehicleKey, Passenger passenger, Locaion locaion) {
        Vehicle vehicle = Vehicle.create(vehicleKey);
        Pickup pickup = Pickup.create(command.requestId(), passenger.name(), locaion, command.destination(), vehicle);
        Completion.create(pickup);
        return pickup;
    }
}