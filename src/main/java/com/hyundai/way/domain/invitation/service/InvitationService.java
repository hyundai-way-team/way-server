package com.hyundai.way.domain.invitation.service;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.invitation.dto.request.ExchangeInvitationCommand;
import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResult;
import com.hyundai.way.domain.invitation.exception.InviationErrorCode;
import com.hyundai.way.domain.invitation.exception.InviationException;
import com.hyundai.way.domain.invitation.repository.InvitationRepository;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.global.secure.AccessCodeGenerator;
import com.hyundai.way.global.secure.ParticipationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final InvitationRepository invitationRepository;
    private final PickupRepository pickupRepository;

    @Transactional
    public ExchangeInvitationResult exchangeInvitationCode(ExchangeInvitationCommand command) {
        String codeLookupKey = ParticipationCode.lookupKey(command.participationCode());
        // TODO: 초대를 조회할 때 비관적 락을 적용해 최초 교환을 한 번만 허용한다.
        Invitation invitation = invitationRepository.findByCodeLookupKey(codeLookupKey)
                .orElseThrow(() -> new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE));

        Long pickupId = invitation.getPickupId();
        Pickup pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE));
        if (pickup.getCompletion() != null) {
            throw new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE);
        }

        if (invitation.isCodeConsumed()) {
            String accessToken = command.accessToken();
            if (accessToken == null || accessToken.isBlank()) {
                throw new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE);
            }
            // TODO: 토큰이 이 초대에 대해 유효한지 확인한다.
            return ExchangeInvitationResult.reentry(pickupId);
        }

        if (isCodeExpired(invitation)) {
            throw new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE);
        }

        invitation.consumingCode();
        String newAccessToken = AccessCodeGenerator.issue(SECURE_RANDOM);
        return ExchangeInvitationResult.firstExchange(pickupId, newAccessToken);
    }

    private boolean isCodeExpired(Invitation invitation) {
        return !invitation.getCodeExpiresAt().isAfter(Instant.now());
    }
}
