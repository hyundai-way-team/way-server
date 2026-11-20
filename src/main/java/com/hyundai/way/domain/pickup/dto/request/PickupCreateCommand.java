package com.hyundai.way.domain.pickup.dto.request;

public record PickupCreateCommand(
        String requestId,
        String vehicleNumber,
        Passenger passenger,
        PickupLocation pickupLocation,
        String destination
) {
    public record Passenger(
            String name,
            String phoneNumber
    ) {}

    public record PickupLocation(
            String address,
            Double latitude,
            Double longitude
    ) {}
}
