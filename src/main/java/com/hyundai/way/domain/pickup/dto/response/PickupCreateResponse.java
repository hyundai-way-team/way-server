package com.hyundai.way.domain.pickup.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PickupCreateResponse(
        @Schema(description = "픽업 조회와 WebSocket 연결 경로에 사용하는 픽업 식별자입니다.",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
        String pickupId,
        @Schema(description = "참여 교환 코드가 #code에 포함된 동승자 웹 초대 링크입니다.",
                requiredMode = Schema.RequiredMode.REQUIRED, example = "https://passenger.example.com/p/join#code=example-code")
        String invitationWebURL
) {
    public static PickupCreateResponse create(Long pickupId, String invitationWebURL) {
        return new PickupCreateResponse(pickupId.toString(), invitationWebURL);
    }
}
