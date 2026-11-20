package com.hyundai.way.domain.vehicle.repository;

import com.hyundai.way.domain.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    // Read the stored key so the caller can compare it independently of the database collation.
    Optional<Vehicle> findByVehicleKey(String vehicleKey);
}
