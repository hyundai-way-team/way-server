package com.hyundai.way.domain.invitation.dto.request;

public record ExchangeInvitationCommand(
        String accessCode,
        String participationCode
) {
}
