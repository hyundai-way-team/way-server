package com.hyundai.way.domain.invitation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExchangeInvitationResponse(
        @Schema(description = "픽업 조회와 WebSocket 연결 경로에 사용하는 픽업 식별자입니다.",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
        String pickupId
) {
    public static ExchangeInvitationResponse create(String pickupId) {
        return new ExchangeInvitationResponse(pickupId);
    }
}
