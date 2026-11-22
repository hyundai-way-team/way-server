package com.hyundai.way.domain.invitation.dto.response;

import org.jspecify.annotations.Nullable;

public record ExchangeInvitationResult(
        String pickupId,
        @Nullable String newAccessToken
) {
    public static ExchangeInvitationResult firstExchange(Long pickupId, String newAccessToken) {
        return new ExchangeInvitationResult(pickupId.toString(), newAccessToken);
    }

    public static ExchangeInvitationResult reentry(Long pickupId) {
        return new ExchangeInvitationResult(pickupId.toString(), null);
    }
}
