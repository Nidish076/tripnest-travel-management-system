package com.tripnest.backend.service;

import com.tripnest.backend.dto.ItineraryRequest;
import com.tripnest.backend.dto.ItineraryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ItineraryService {
    ItineraryResponse addItineraryItem(Long tripId, ItineraryRequest request, Long currentUserId);
    List<ItineraryResponse> getItinerary(Long tripId, Long currentUserId);
    List<ItineraryResponse> getItineraryByDate(Long tripId, LocalDate date, Long currentUserId);
    ItineraryResponse updateItineraryItem(Long itemId, ItineraryRequest request, Long currentUserId);
    void deleteItineraryItem(Long itemId, Long currentUserId);
}
