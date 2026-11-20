package com.hyundai.way.domain.pickup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Locaion {

    @Column(name = "pickup_address", nullable = false)
    private String address;

    @Column(name = "pickup_latitude", nullable = false)
    private Double latitude;

    @Column(name = "pickup_longitude", nullable = false)
    private Double longitude;

    public static Locaion create(String address, Double latitude, Double longitude) {
        return new Locaion(address, latitude, longitude);
    }
}
