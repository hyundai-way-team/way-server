package com.hyundai.way.domain.invitation.dto.response;

public record ExchangeInvitationResponse(
        String pickupId
) {
    public static ExchangeInvitationResponse create(String pickupId) {
        return new ExchangeInvitationResponse(pickupId);
    }
}
