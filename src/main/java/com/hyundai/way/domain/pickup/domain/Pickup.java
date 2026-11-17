package com.hyundai.way.domain.pickup.domain;

import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Table(name = "pickup")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pickup extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Embedded
    private Locaion pickupLocation;

    @Column(name = "destination_address")
    private String destination;

    @Column(name = "waiting_ends_at")
    private Instant waitingEndsAt;

    //=== Constructor ===
    @Builder(access = AccessLevel.PRIVATE)
    public Pickup(String requestId, String passengerName, Locaion pickupLocation, String destination, Instant waitingEndsAt) {
        this.requestId = requestId;
        this.passengerName = passengerName;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.waitingEndsAt = waitingEndsAt;
    }

    public static Pickup create(
            String requestId, String passengerName, Locaion pickupLocation, String destination, Instant waitingEndsAt) {
        return Pickup.builder()
                .requestId(requestId)
                .passengerName(passengerName)
                .pickupLocation(pickupLocation)
                .destination(destination)
                .waitingEndsAt(waitingEndsAt)
                .build();
    }

    //=== Domain Polish ===


}
