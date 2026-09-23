package com.tripnest.backend.repository;

import com.tripnest.backend.model.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    List<TripMember> findByTripId(Long tripId);

    Optional<TripMember> findByTripIdAndUserId(Long tripId, Long userId);

    Boolean existsByTripIdAndUserId(Long tripId, Long userId);

    Integer countByTripId(Long tripId);

    void deleteByTripIdAndUserId(Long tripId, Long userId);
}
