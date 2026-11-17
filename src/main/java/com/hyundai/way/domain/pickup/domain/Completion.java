package com.hyundai.way.domain.pickup.domain;

import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Column(name = "pickup_id", nullable = false)
    private Long pickupId;

    private Completion(Long pickupId) {
        this.pickupId = pickupId;
    }

    public static Completion create(Long pickupId) {
        return new Completion(pickupId);
    }
}
