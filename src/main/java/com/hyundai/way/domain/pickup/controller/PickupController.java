package com.hyundai.way.domain.pickup.controller;

import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateRequest;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResponse;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.service.PickupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(PickupController.PICKUPS_PATH)
@RequiredArgsConstructor
public class PickupController {

    static final String PICKUPS_PATH = "/api/v1/pickups";

    private final PickupService pickupService;

    @PostMapping
    public ResponseEntity<PickupCreateResponse> create(
            @RequestHeader("VIN") @NotBlank String vin,
            @Valid @RequestBody PickupCreateRequest request) {
        PickupCreateCommand command = request.toCommand();
        PickupCreateResult result = pickupService.createPickup(command, vin);

        URI location = URI.create(PICKUPS_PATH + "/" + result.pickupId());
        return ResponseEntity.created(location)
                .body(PickupCreateResponse.create(result.pickupId(), result.invitationWebURL()));
    }
}
