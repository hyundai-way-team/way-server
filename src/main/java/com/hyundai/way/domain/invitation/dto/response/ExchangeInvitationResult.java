package com.hyundai.way.domain.invitation.dto.response;

public record ExchangeInvitationResult(
        String accessCode,
        String pickupId
) {
    public static ExchangeInvitationResult from(String accessCode, Long pickupId) {
        return new ExchangeInvitationResult(accessCode, pickupId.toString());
    }
}
