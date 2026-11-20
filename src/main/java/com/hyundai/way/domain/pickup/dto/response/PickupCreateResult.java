package com.hyundai.way.domain.pickup.dto.response;

public record PickupCreateResult(
        String pickupId,
        String invitationWebURL
) {
    public static PickupCreateResult from(String requestId, String invitationWebURL) {
        return new PickupCreateResult(requestId, invitationWebURL);
    }
}
