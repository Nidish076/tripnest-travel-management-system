package com.tripnest.backend.repository;

import com.tripnest.backend.model.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItineraryRepository extends JpaRepository<ItineraryItem, Long> {

    List<ItineraryItem> findByTripIdOrderByDateAscStartTimeAsc(Long tripId);

    List<ItineraryItem> findByTripIdAndDateOrderByStartTimeAsc(Long tripId, LocalDate date);

    List<ItineraryItem> findByTripId(Long tripId);
}