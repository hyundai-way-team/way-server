package com.hyundai.way.domain.pickup.dto.response;

public record PickupCreateResponse(
        Long pickupId,
        String invitationWebURL
) {
    public static PickupCreateResponse create(Long pickupId, String invitationWebURL) {
        return new PickupCreateResponse(pickupId, invitationWebURL);
    }
}