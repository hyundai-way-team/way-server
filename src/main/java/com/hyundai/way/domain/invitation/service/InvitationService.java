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
    private final PassengerAccessTokenService passengerAccessTokenService;

    @Transactional
    public ExchangeInvitationResult exchangeInvitationCode(ExchangeInvitationCommand command) {
        String codeLookupKey = ParticipationCode.lookupKey(command.participationCode());

        Invitation invitation = invitationRepository.findByCodeLookupKeyForUpdate(codeLookupKey)
                .orElseThrow(() -> new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE));

        Long pickupId = invitation.getPickupId();

        Pickup pickup = getPickup(pickupId);

        checkPickupNotCompleted(pickup);

        if (invitation.isCodeConsumed()) {
            String accessToken = command.accessToken();
            requireReentryToken(accessToken);
            if (!passengerAccessTokenService.isValidFor(accessToken, pickupId)) {
                throw new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE);
            }

            return ExchangeInvitationResult.reentry(pickupId);
        }

        validCodeExpired(isCodeExpired(invitation));

        invitation.consumingCode();
        String newAccessToken = passengerAccessTokenService.issue(pickupId);
        return ExchangeInvitationResult.firstExchange(pickupId, newAccessToken);
    }

    private void requireReentryToken(String accessToken) {
        validCodeExpired(accessToken == null || accessToken.isBlank());
    }

    private void checkPickupNotCompleted(Pickup pickup) {
        validCodeExpired(pickup.getCompletion() != null);
    }

    private void validCodeExpired(boolean invitation) {
        if (invitation) {
            throw new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE);
        }
    }

    private Pickup getPickup(Long pickupId) {
        return pickupRepository.findById(pickupId)
                .orElseThrow(() -> new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE));
    }

    private boolean isCodeExpired(Invitation invitation) {
        return !invitation.getCodeExpiresAt().isAfter(Instant.now());
    }
}
