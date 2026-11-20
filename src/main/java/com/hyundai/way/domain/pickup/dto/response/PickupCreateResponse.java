package com.hyundai.way.domain.pickup.dto.response;

public record PickupCreateResponse(
        String pickupId,
        String invitationWebURL
) {
    public static PickupCreateResponse create(String pickupId, String invitationWebURL) {
        return new PickupCreateResponse(pickupId, invitationWebURL);
    }
}