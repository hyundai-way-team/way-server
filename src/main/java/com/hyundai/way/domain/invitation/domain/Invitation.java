package com.hyundai.way.domain.invitation.domain;

import com.hyundai.way.domain.invitation.exception.InviationErrorCode;
import com.hyundai.way.domain.invitation.exception.InviationException;
import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
    @Column(nullable = false)
    private Long pickupId;

    @Column(name = "code_lookup_key", nullable = false, unique = true)
    private String codeLookupKey;

    @Column(name = "code_expires_at", nullable = false)
    private Instant codeExpiresAt;

    @Column(name = "code_consumed", nullable = false)
    private boolean codeConsumed;

    @MapsId
    @OneToOne
    @JoinColumn(name = "pickup_id")
    private Pickup pickup;

    @Builder(access = AccessLevel.PRIVATE)
    private Invitation(Pickup pickup, String codeLookupKey, Instant codeExpiresAt, Boolean codeConsumed) {
        this.pickup = pickup;
        this.codeLookupKey = codeLookupKey;
        this.codeExpiresAt = codeExpiresAt;
        this.codeConsumed = codeConsumed;
    }

    public static Invitation create(Pickup pickup, String codeLookupKey, Instant codeExpiresAt) {
        Invitation invitation = Invitation.builder()
                .pickup(pickup)
                .codeLookupKey(codeLookupKey)
                .codeExpiresAt(codeExpiresAt)
                .codeConsumed(false)
                .build();

        invitation.connectWithPickup(pickup);

        return invitation;
    }

    //=== Util Method ===
    public void connectWithPickup(Pickup pickup) {
        pickup.connectWithInvitation(this);

        //TODO: 이미 픽업이 연결된 초대링크인 경우 예외 처리 필요

        this.pickup = pickup;
    }

    //=== Domain Polish ===

    public void consumingCode() {
        if(codeConsumed) throw new InviationException(InviationErrorCode.ALREADY_CONSUMED_CODE);

        this.codeConsumed = true;
    }

}
