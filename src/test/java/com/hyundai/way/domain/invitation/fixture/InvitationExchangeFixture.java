package com.hyundai.way.domain.invitation.fixture;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.invitation.service.InvitationServiceConcurrencyTest;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.global.secure.ParticipationCode;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class InvitationExchangeFixture {

    public static ExchangeFixture saveExchangeFixture(PlatformTransactionManager transactionManager, EntityManager em) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        log.info("픽업·초대 준비 시작");
        ExchangeFixture fixture = Objects.requireNonNull(transaction.execute(status -> {
            String fixtureId = UUID.randomUUID().toString();
            String participationCode = ParticipationCode.generate(new SecureRandom());

            Vehicle vehicle = Vehicle.create(fixtureId);
            em.persist(vehicle);

            Pickup pickup = Pickup.create(
                    fixtureId,
                    "테스트 동승자",
                    Locaion.create("테스트 픽업 장소", 37.59, 127.02),
                    null,
                    vehicle
            );

            Invitation.create(
                    pickup, ParticipationCode.lookupKey(participationCode), Instant.now().plusSeconds(600));

            em.persist(pickup);
            em.flush();
            log.info("픽업·초대 flush 완료, 커밋 전 pickupId={}", pickup.getId());

            return new ExchangeFixture(pickup.getId(), participationCode);
        }));
        log.info("픽업·초대 커밋 완료 pickupId={}", fixture.pickupId());
        return fixture;
    }

    public record ExchangeFixture(Long pickupId, String participationCode) {}
}
