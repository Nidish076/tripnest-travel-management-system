package com.tripnest.backend.service;

import com.tripnest.backend.entity.Itinerary;
import com.tripnest.backend.entity.Trip;

import java.util.List;

public interface GroupTripService {
    void shareTrip(Long tripId, Long groupId);
    void unshareTrip(Long tripId);
    List<Trip> getGroupTrips(Long groupId);
    List<Itinerary> getGroupTripItineraries(Long groupId, Long tripId);
}
