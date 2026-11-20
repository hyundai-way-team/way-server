package com.hyundai.way.domain.pickup.domain;

import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "completion")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Completion extends BaseEntity {

    @Id
    @Column(nullable = false)
    private Long pickupId;

    @MapsId
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "pickup_id")
    private Pickup pickup;

    private Completion(Pickup pickup) {
        this.pickup = pickup;
        pickup.connectWithCompletion(this);
    }

    public static Completion create(Pickup pickup) {
        return new Completion(pickup);
    }
}
