package com.hyundai.way.domain.invitation.controller;

import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResult;
import com.hyundai.way.domain.invitation.service.InvitationService;
import com.hyundai.way.domain.invitation.dto.request.ExchangeInvitationCommand;
import com.hyundai.way.domain.invitation.dto.request.ExchangeInvitationRequest;
import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/invitations/exchange")
    public ExchangeInvitationResponse exchangeInvitationCode(
            @RequestHeader("ACCESS_TOKEN") @NotBlank String accessToken,
            @Valid @RequestBody ExchangeInvitationRequest request) {
        ExchangeInvitationCommand command = request.toCommand(accessToken);
        ExchangeInvitationResult result = invitationService.exchangeInvitationCode(command);

        return ExchangeInvitationResponse.create(result.pickupId());
    }
}
