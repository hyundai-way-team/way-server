package com.hyundai.way.domain.pickup.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PickupCreateRequest(
        @NotBlank @Size(max = 255) String requestId,
        String vehicleNumber,
        @NotNull @Valid Passenger passenger,
        @NotNull @Valid PickupLocation pickupLocation,
        @Size(max = 255) String destination
) {

    public PickupCreateCommand toCommand() {
        return new PickupCreateCommand(
                requestId,
                vehicleNumber,
                new PickupCreateCommand.Passenger(passenger.name(), passenger.phoneNumber()),
                new PickupCreateCommand.PickupLocation(
                        pickupLocation.address(), pickupLocation.latitude(), pickupLocation.longitude()),
                destination
        );
    }

    public record Passenger(
            @NotBlank @Size(max = 255) String name,
            String phoneNumber
    ) {
    }

    public record PickupLocation(
            @NotBlank @Size(max = 255) String address,
            @NotNull
            @DecimalMin(value = "-90", message = "위도는 -90에서 90 사이여야 합니다.")
            @DecimalMax(value = "90", message = "위도는 -90에서 90 사이여야 합니다.")
            Double latitude,
            @NotNull
            @DecimalMin(value = "-180", message = "경도는 -180에서 180 사이여야 합니다.")
            @DecimalMax(value = "180", message = "경도는 -180에서 180 사이여야 합니다.")
            Double longitude
    ) {
    }
}
