package com.hyundai.way.domain.vehicle.repository;

import com.hyundai.way.domain.vehicle.domain.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select vehicle from Vehicle vehicle where vehicle.vehicleKey = :vehicleKey")
    Optional<Vehicle> findByVehicleKeyForUpdate(@Param("vehicleKey") String vehicleKey);
}
