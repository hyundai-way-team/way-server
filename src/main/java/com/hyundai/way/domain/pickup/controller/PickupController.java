package com.hyundai.way.domain.pickup.controller;

import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateRequest;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResponse;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.service.PickupService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/pickups")
@RequiredArgsConstructor
public class PickupController {

    private final PickupService pickupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PickupCreateResponse create(
            @RequestHeader("VIN") @NotBlank String vin,
            @RequestBody PickupCreateRequest request) {
        PickupCreateCommand command = request.toCommand();
        PickupCreateResult result = pickupService.createPickup(command, vin);

        return PickupCreateResponse.create(result.pickupId(), result.invitationWebURL());
    }
}
