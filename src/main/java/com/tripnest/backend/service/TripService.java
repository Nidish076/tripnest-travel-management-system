package com.tripnest.backend.service;

import com.tripnest.backend.dto.CreateTripRequest;
import com.tripnest.backend.dto.TripBalancesResponse;
import com.tripnest.backend.dto.TripResponse;
import com.tripnest.backend.dto.UpdateTripRequest;

import java.util.List;

public interface TripService {
    TripResponse createTrip(CreateTripRequest request, Long currentUserId);
    List<TripResponse> getUserTrips(Long currentUserId);
    TripResponse getTripById(Long tripId, Long currentUserId);
    TripResponse updateTrip(Long tripId, UpdateTripRequest request, Long currentUserId);
    void deleteTrip(Long tripId, Long currentUserId);
    TripBalancesResponse getTripBalances(Long tripId, Long currentUserId);
}
