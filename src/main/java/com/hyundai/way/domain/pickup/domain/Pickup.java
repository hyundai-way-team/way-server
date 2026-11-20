package com.hyundai.way.domain.pickup.domain;

import com.hyundai.way.domain.invitation.domain.Invitation;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Table(
        name = "pickup",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pickup_vehicle_key_request_id",
                        columnNames = {"vehicle_key", "request_id"}
                )
        }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pickup extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "pickup_id")
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_key", nullable = false)
    private Vehicle vehicle;

    @OneToOne(mappedBy = "pickup", cascade = CascadeType.PERSIST)
    private Invitation invitation;

    @OneToOne(mappedBy = "pickup", cascade = CascadeType.PERSIST)
    private Completion completion;

    //=== Constructor ===
    @Builder(access = AccessLevel.PRIVATE)
    private Pickup(String requestId, String passengerName, Locaion pickupLocation,
                   String destination, Instant waitingEndsAt) {
        this.requestId = requestId;
        this.passengerName = passengerName;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.waitingEndsAt = waitingEndsAt;
    }

    public static Pickup create(
            String requestId, String passengerName, Locaion pickupLocation,
            String destination, Vehicle vehicle) {
        Pickup build = Pickup.builder()
                .requestId(requestId)
                .passengerName(passengerName)
                .pickupLocation(pickupLocation)
                .destination(destination)
                .build();

        build.connectWithVehicle(vehicle);

        return build;
    }

    //=== Util Method ==

    public void connectWithVehicle(Vehicle vehicle) {
        vehicle.getPickups().add(this);
        this.vehicle = vehicle;
    }

    public void connectWithInvitation(Invitation invitation) {
        Objects.requireNonNull(invitation, "invitation is not null");
        this.invitation = invitation;
    }

    public void connectWithCompletion(Completion completion) {
        Objects.requireNonNull(completion, "completion is not null");
        this.completion = completion;
    }

    //=== Domain Polish ===

    //TODO: waitingEndsAt 추가 메서드 제작

}
