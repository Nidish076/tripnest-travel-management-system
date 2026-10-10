package com.tripnest.backend.controller;

import com.tripnest.backend.dto.response.ApiResponse;
import com.tripnest.backend.entity.Itinerary;
import com.tripnest.backend.entity.Trip;
import com.tripnest.backend.service.GroupTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupTripController {

    private final GroupTripService groupTripService;

    @PutMapping("/api/trips/{tripId}/share/{groupId}")
    public ResponseEntity<ApiResponse<Void>> shareTrip(@PathVariable Long tripId, @PathVariable Long groupId) {
        groupTripService.shareTrip(tripId, groupId);
        return ResponseEntity.ok(ApiResponse.success("Trip shared successfully", null));
    }

    @DeleteMapping("/api/trips/{tripId}/share")
    public ResponseEntity<ApiResponse<Void>> unshareTrip(@PathVariable Long tripId) {
        groupTripService.unshareTrip(tripId);
        return ResponseEntity.ok(ApiResponse.success("Trip unshared successfully", null));
    }

    @GetMapping("/api/groups/{groupId}/trips")
    public ResponseEntity<ApiResponse<List<Trip>>> getGroupTrips(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupTripService.getGroupTrips(groupId)));
    }

    @GetMapping("/api/groups/{groupId}/trips/{tripId}/itineraries")
    public ResponseEntity<ApiResponse<List<Itinerary>>> getGroupTripItineraries(
            @PathVariable Long groupId, 
            @PathVariable Long tripId) {
        return ResponseEntity.ok(ApiResponse.success(groupTripService.getGroupTripItineraries(groupId, tripId)));
    }
}
