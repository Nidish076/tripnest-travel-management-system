package com.tripnest.backend.repository;

import com.tripnest.backend.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findByInviteCode(String inviteCode);

    Boolean existsByInviteCode(String inviteCode);

    @Query("SELECT DISTINCT t FROM Trip t JOIN t.members m WHERE m.user.id = :userId ORDER BY t.createdAt DESC")
    List<Trip> findTripsByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Trip t JOIN t.members m WHERE t.id = :tripId AND m.user.id = :userId")
    Optional<Trip> findByIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);
}