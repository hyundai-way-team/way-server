package com.hyundai.way.domain.vehicle.domain;

import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Table(name = "vehicle")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle extends BaseEntity {

    @Id
    @Column(name = "vehicle_key", nullable = false)
    private String vehicleKey;

    @OneToMany(mappedBy = "vehicle")
    private List<Pickup> pickups = new ArrayList<>();

    private Vehicle(String vehicleKey) {
        this.vehicleKey = vehicleKey;
    }

    public static Vehicle create(String vehicleKey) {
        return new Vehicle(vehicleKey);
    }
}
