package com.hyundai.way.domain.invitation.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class PassengerAccessTokenService {

    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;
    private static final String ISSUER = "way-passenger";

    private final MACSigner signer;
    private final MACVerifier verifier;
    private final Duration ttl;

    public PassengerAccessTokenService(
            @Value("${way.invitation.access-token-secret}") String secretBase64,
            @Value("${way.invitation.access-token-ttl}") String ttl
    ) throws JOSEException {
        byte[] secret = Base64.getDecoder().decode(secretBase64);
        if (secret.length < 32) {
            throw new IllegalArgumentException("서명 키는 디코딩 된 후 32바이트 이상이어야 합니다.");
        }

        this.ttl = Duration.parse(ttl);
        if (this.ttl.isZero() || this.ttl.isNegative()) {
            throw new IllegalArgumentException("동승자 토큰의 유효 기간은 양수여야 합니다.");
        }

        this.signer = new MACSigner(secret);
        this.verifier = new MACVerifier(secret);
    }

    public String issue(Long pickupId) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(pickupId.toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(ALGORITHM), claims);

        try {
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("동승자 토큰 발급에 실패했습니다.", e);
        }
    }

    public boolean isValidFor(@Nullable String accessToken, Long pickupId) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        SignedJWT jwt = parsePassengerToken(accessToken);
        if (jwt == null) return false;

        return isValidPassengerToken(pickupId, jwt);
    }

    private boolean isValidPassengerToken(Long pickupId, SignedJWT jwt) {
        try {
            if (!ALGORITHM.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(verifier)) {
                return false;
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiredAt = claims.getExpirationTime();

            return ISSUER.equals(claims.getIssuer())
                    && pickupId.toString().equals(claims.getSubject())
                    && expiredAt != null
                    && expiredAt.toInstant().isAfter(Instant.now());
        } catch (ParseException ignored) {
            log.debug("동승자 토큰 클레임 파싱에 실패했습니다.");
            return false;
        } catch (JOSEException exception) {
            throw new IllegalStateException("동승자 토큰 서명을 검증하지 못했습니다.", exception);
        }
    }

    private SignedJWT parsePassengerToken(String accessToken) {
        try {
            // Nimbus 10.10 파싱 우회. 수정 버전에서 ParseException으로 처리되면 NPE catch를 제거한다.
            return SignedJWT.parse(accessToken);
        } catch (ParseException | NullPointerException ignored) {
            log.debug("동승자 토큰 파싱에 실패했습니다.");
            return null;
        }
    }

}
