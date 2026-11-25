package com.hyundai.way.domain.invitation.controller;

import com.hyundai.way.domain.invitation.controller.docs.ExchangeInvitationDocs;
import com.hyundai.way.domain.invitation.dto.request.ExchangeInvitationCommand;
import com.hyundai.way.domain.invitation.dto.request.ExchangeInvitationRequest;
import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResponse;
import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResult;
import com.hyundai.way.domain.invitation.service.InvitationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "동승자 초대 API", description = "동승자가 참여 코드를 교환하는 API")
@RestController
@RequestMapping("/api/v1/pickups/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final InvitationService invitationService;

    @ExchangeInvitationDocs
    @PostMapping("/exchange")
    public ResponseEntity<ExchangeInvitationResponse> exchangeInvitationCode(
            @CookieValue(name = ACCESS_TOKEN_COOKIE, required = false) @Nullable String accessToken,
            @Valid @RequestBody ExchangeInvitationRequest request) {
        ExchangeInvitationCommand command = request.toCommand(accessToken);
        ExchangeInvitationResult result = invitationService.exchangeInvitationCode(command);

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        String newAccessToken = result.newAccessToken();
        setAccessTokenCookie(newAccessToken, response);

        return response.body(ExchangeInvitationResponse.create(result.pickupId()));
    }

    private void setAccessTokenCookie(String newAccessToken, ResponseEntity.BodyBuilder response) {
        if (newAccessToken != null) {
            ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, newAccessToken)
                    .path("/")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .build();
            response.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }
}
