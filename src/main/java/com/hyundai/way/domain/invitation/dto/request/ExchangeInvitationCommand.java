package com.hyundai.way.domain.invitation.dto.request;

import org.jspecify.annotations.Nullable;

public record ExchangeInvitationCommand(
        @Nullable String accessToken,
        String participationCode
) {
}
