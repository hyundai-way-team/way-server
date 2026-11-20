package com.hyundai.way.domain.pickup.repository;

import com.hyundai.way.domain.pickup.domain.Pickup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickupRepository extends JpaRepository<Pickup, Long> {

    List<Pickup> findByRequestId(String requestId);
}
