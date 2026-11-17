package com.hyundai.way.domain.invitation.domain;

import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Table(name = "invitation")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseEntity {

    @Id
    @Column(name = "pickup_id", nullable = false)
    private Long pickupId;

    @Column(name = "code_lookup_key", nullable = false, unique = true)
    private String codeLookupKey;

    @Column(name = "code_expires_at", nullable = false)
    private Instant codeExpiresAt;

    @Column(name = "code_consumed", nullable = false)
    private boolean codeConsumed;

    @Builder(access = AccessLevel.PRIVATE)
    private Invitation(Long pickupId, String codeLookupKey, Instant codeExpiresAt) {
        this.pickupId = pickupId;
        this.codeLookupKey = codeLookupKey;
        this.codeExpiresAt = codeExpiresAt;
    }

    public static Invitation create(Long pickupId, String codeLookupKey, Instant codeExpiresAt) {
        return Invitation.builder()
                .pickupId(pickupId)
                .codeLookupKey(codeLookupKey)
                .codeExpiresAt(codeExpiresAt)
                .build();
    }
}
