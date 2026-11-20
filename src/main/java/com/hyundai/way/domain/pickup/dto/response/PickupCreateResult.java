package com.hyundai.way.domain.pickup.dto.response;

public record PickupCreateResult(
        Long pickupId,
        String invitationWebURL
) {
    public static PickupCreateResult from(Long requestId, String invitationWebURL) {
        return new PickupCreateResult(requestId, invitationWebURL);
    }
}
