package com.hyundai.way.domain.pickup.controller;

import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateRequest;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResponse;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.service.PickupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/pickups")
@RequiredArgsConstructor
public class PickupController {

    private final PickupService pickupService;

    @PostMapping
    public PickupCreateResponse create(
            @RequestBody PickupCreateRequest request) {
        PickupCreateCommand command = request.toCommand();
        PickupCreateResult result = pickupService.createPickup(command, "vehicleKey");

        return PickupCreateResponse.create(result.pickupId(), result.invitationWebURL());
    }
}
