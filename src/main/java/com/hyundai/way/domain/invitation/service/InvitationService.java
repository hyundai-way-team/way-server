package com.hyundai.way.domain.invitation.service;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.invitation.dto.request.ExchangeInvitationCommand;
import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResult;
import com.hyundai.way.domain.invitation.exception.InviationErrorCode;
import com.hyundai.way.domain.invitation.exception.InviationException;
import com.hyundai.way.domain.invitation.repository.InvitationRepository;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.domain.vehicle.exception.VehicleErrorCode;
import com.hyundai.way.domain.vehicle.exception.VehicleException;
import com.hyundai.way.domain.vehicle.repository.VehicleRepository;
import com.hyundai.way.global.secure.AccessCodeGenerator;
import com.hyundai.way.global.secure.ParticipationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final InvitationRepository invitationRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public ExchangeInvitationResult exchangeInvitationCode(ExchangeInvitationCommand command) {
        // 0. 등록된 차량인지 확인한다.
        // 1. 교환 가능한 초대 코드인지 확인
        // 1-1. 이미 사용한 코드인 경우 기존 pickupId 반환
        // 1-2. 등록되지 않은 코드, 만료된 미사용 코드, 유효한 픽업 세션이 없는 경우 에러를 던진다.(403 Forbidden)
        // 2. 교환 가능한 코드는 소비하고 access_token과 pickupId를 반환한다.
        // 2-1. 초대 코드 교환시 비관적 락을 통해 레이스 컨디션을 방지한다.
        // (네트워크 지연 등의 이유로 동시에 새 링크를 열 때 레이스 컨디션 발생 가능성 높음. )

        String codeLookupKey = ParticipationCode.lookupKey(command.participationCode());
        Invitation invitation = invitationRepository.findByCodeLookupKey(codeLookupKey)
                .orElseThrow(() -> new InviationException(InviationErrorCode.INVITATION_UNAVAILABLE_CODE));

        Long pickupId = invitation.getPickupId();

        boolean codeConsumed = invitation.isCodeConsumed();

        if (codeConsumed) {
            return ExchangeInvitationResult.from(command.accessCode(), pickupId);
        }

        invitation.consumingCode();
        String accessToken = AccessCodeGenerator.issue(SECURE_RANDOM);

        return ExchangeInvitationResult.from(accessToken, pickupId);
    }

    private Vehicle findRegisteredVehicleForUpdate(String vin) {
        return vehicleRepository.findByVehicleKeyForUpdate(vin)
                .filter(registeredVehicle -> vin.equals(registeredVehicle.getVehicleKey()))
                .orElseThrow(() -> new VehicleException(VehicleErrorCode.VEHICLE_NOT_REGISTERED));
    }
}
