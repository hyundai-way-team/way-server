package com.hyundai.way.domain.pickup.repository;

import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickupRepository extends JpaRepository<Pickup, Long> {

    Optional<Pickup> findByVehicleAndRequestId(Vehicle vehicle, String requestId);
}
