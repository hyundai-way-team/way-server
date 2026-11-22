package com.hyundai.way.domain.invitation.dto.request;

public record ExchangeInvitationRequest(
        String participationCode
) {
    public ExchangeInvitationCommand toCommand(String accessCode) {
        return new ExchangeInvitationCommand(accessCode, participationCode);
    }
}
