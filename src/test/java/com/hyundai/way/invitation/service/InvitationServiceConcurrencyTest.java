package com.hyundai.way.invitation.service;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.invitation.repository.InvitationRepository;
import com.hyundai.way.domain.pickup.domain.Locaion;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.global.secure.ParticipationCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class InvitationServiceConcurrencyTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvitationRepository invitationRepository;

    private ExchangeFixture saveExchangeFixture() {
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

    private record ExchangeFixture(Long pickupId, String participationCode) {}

    @Test
    @DisplayName("최초 초대 교환시 레이스 컨디션을 방지하여 처음 요청된 것에 대해서만 코드를 교환한다.")
    void 같은_참여_코드의_최초_교환은_한_건만_성공한다() throws Exception {
        //given
        ExchangeFixture fixture = saveExchangeFixture();
        String body = """
            {"participationCode": "%s"}
            """.formatted(fixture.participationCode());

        ExecutorService pool = newFixedThreadPool(2);
        CyclicBarrier start = new CyclicBarrier(2, () -> log.info("두 작업 도착, 배리어 해제"));

        //when

        Callable<MockHttpServletResponse> exchange = () -> {
            var request = MockMvcRequestBuilders.post("/api/v1/pickups/invitations/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body);

            String worker = Thread.currentThread().getName();
            log.info("[{}] 배리어 도착", worker);
            start.await(5, TimeUnit.SECONDS);

            log.info("[{}] 교환 요청 시작", worker);
            MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
            log.info("[{}] 교환 응답 status={}", worker, response.getStatus());
            return response;
        };

        //then
        try {
            var first = pool.submit(exchange);
            var second = pool.submit(exchange);

            var responses = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)
            );

            assertThat(responses).anySatisfy(response -> {
                assertThat(response.getStatus()).isEqualTo(200);
                assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                        .startsWith("access_token=");
            });

            assertThat(responses).anySatisfy(response -> {
                assertThat(response.getStatus()).isEqualTo(403);
                assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
            });
        } finally {
            pool.shutdown();
        }

    }
}
