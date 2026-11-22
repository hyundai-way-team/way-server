package com.hyundai.way.domain.invitation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record ExchangeInvitationRequest(
        @NotBlank @Size(max = 255) String participationCode
) {
    public ExchangeInvitationCommand toCommand(@Nullable String accessToken) {
        return new ExchangeInvitationCommand(accessToken, participationCode);
    }
}
