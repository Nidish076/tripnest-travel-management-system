package com.tripnest.backend.repository;

import com.tripnest.backend.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByTripIdOrderByCreatedAtDesc(Long tripId);

    List<Settlement> findByTripId(Long tripId);
}
