package com.hyundai.way.domain.pickup.service;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand.PickupLocation;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.exception.PickupErrorCode;
import com.hyundai.way.domain.pickup.exception.PickupException;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.pickup.secure.ParticipationCode;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.domain.vehicle.exception.VehicleErrorCode;
import com.hyundai.way.domain.vehicle.exception.VehicleException;
import com.hyundai.way.domain.vehicle.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PickupService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration INVITATION_VALIDITY = Duration.ofMinutes(30);

    private final PickupRepository pickupRepository;
    private final VehicleRepository vehicleRepository;
    private final String invitationWebBaseUrl;
    private final ConcurrentMap<Long, InvitationLink> invitationLinks = new ConcurrentHashMap<>();

    public PickupService(PickupRepository pickupRepository, VehicleRepository vehicleRepository,
                         @Value("${way.pickup.invitation-web-base-url}") String invitationWebBaseUrl) {
        this.pickupRepository = pickupRepository;
        this.vehicleRepository = vehicleRepository;
        this.invitationWebBaseUrl = invitationWebBaseUrl;
    }

    @Transactional
    public PickupCreateResult createPickup(PickupCreateCommand command, String vin) {
        Vehicle vehicle = findRegisteredVehicleForUpdate(vin);
        return pickupRepository.findByVehicleAndRequestId(vehicle, command.requestId())
                .map(pickup -> reuseInvitationLink(pickup.getId()))
                .orElseGet(() -> createPickupWithInvitation(command, vehicle));
    }

    private Vehicle findRegisteredVehicleForUpdate(String vin) {
        return vehicleRepository.findByVehicleKeyForUpdate(vin)
                .filter(registeredVehicle -> vin.equals(registeredVehicle.getVehicleKey()))
                .orElseThrow(() -> new VehicleException(VehicleErrorCode.VEHICLE_NOT_REGISTERED));
    }

    private PickupCreateResult createPickupWithInvitation(PickupCreateCommand command, Vehicle vehicle) {
        String participationCode = ParticipationCode.generate(SECURE_RANDOM);
        String invitationUrl = createInvitationUrl(participationCode);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(INVITATION_VALIDITY);

        PickupLocation location = command.pickupLocation();
        Locaion pickupLocation = Locaion.create(location.address(), location.latitude(), location.longitude());
        Pickup pickup = Pickup.create(command.requestId(), command.passenger().name(), pickupLocation,
                command.destination(), vehicle);
        Invitation.create(pickup, ParticipationCode.lookupKey(participationCode), expiresAt);

        Long pickupId = pickupRepository.save(pickup).getId();
        invitationLinks.values().removeIf(link -> !link.expiresAt().isAfter(now));
        rememberInvitationLink(pickupId, new InvitationLink(invitationUrl, expiresAt));
        return PickupCreateResult.from(pickupId, invitationUrl);
    }

    private PickupCreateResult reuseInvitationLink(Long pickupId) {
        InvitationLink link = invitationLinks.get(pickupId);
        if (link == null) {
            throw new PickupException(PickupErrorCode.INVITATION_LINK_UNAVAILABLE);
        }
        if (!link.expiresAt().isAfter(Instant.now())) {
            invitationLinks.remove(pickupId, link);
            throw new PickupException(PickupErrorCode.INVITATION_LINK_UNAVAILABLE);
        }
        return PickupCreateResult.from(pickupId, link.url());
    }

    private String createInvitationUrl(String participationCode) {
        URI baseUrl = URI.create(invitationWebBaseUrl);
        if (baseUrl.getHost() == null || baseUrl.getUserInfo() != null
                || !("https".equalsIgnoreCase(baseUrl.getScheme()) || "http".equalsIgnoreCase(baseUrl.getScheme()))) {
            throw new IllegalStateException("way.pickup.invitation-web-base-url에 동승자 웹 주소를 설정해야 합니다.");
        }
        return baseUrl.resolve("/p/join#code=" + participationCode).toString();
    }

    private void rememberInvitationLink(Long pickupId, InvitationLink link) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                invitationLinks.put(pickupId, link);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    invitationLinks.remove(pickupId, link);
                }
            }
        });
    }

    private record InvitationLink(String url, Instant expiresAt) {}
}
