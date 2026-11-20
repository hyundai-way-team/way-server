package com.hyundai.way.domain.pickup.dto.request;

public record PickupCreateRequest(
        String requestId,
        PickupCreateCommand.Passenger passenger,
        PickupCreateCommand.PickupLocation pickupLocation,
        String destination
) {

    public PickupCreateCommand toCommand() {
        return new PickupCreateCommand(
                this.requestId,
                this.passenger,
                this.pickupLocation,
                this.destination
        );
    }

    public record Passenger(
            String vehicleNumber,
            String name,
            String phoneNumber
    ) {
    }

    public record PickupLocation(
            String address,
            Double latitude,
            Double longitude
    ) {
    }
}
