package com.tripnest.backend.controller;

import com.tripnest.backend.dto.*;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(@Valid @RequestBody CreateTripRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new com.tripnest.backend.exception.UnauthorizedException("User is not authenticated"));

        TripResponse response = tripService.createTrip(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TripResponse>>> getUserTrips() {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new com.tripnest.backend.exception.UnauthorizedException("User is not authenticated"));

        List<TripResponse> response = tripService.getUserTrips(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("User trips retrieved successfully", response));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new com.tripnest.backend.exception.UnauthorizedException("User is not authenticated"));

        TripResponse response = tripService.getTripById(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip retrieved successfully", response));
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateTripRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new com.tripnest.backend.exception.UnauthorizedException("User is not authenticated"));

        TripResponse response = tripService.updateTrip(tripId, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip updated successfully", response));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new com.tripnest.backend.exception.UnauthorizedException("User is not authenticated"));

        tripService.deleteTrip(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully"));
    }

    @GetMapping("/{tripId}/balances")
    public ResponseEntity<ApiResponse<TripBalancesResponse>> getTripBalances(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new com.tripnest.backend.exception.UnauthorizedException("User is not authenticated"));

        TripBalancesResponse response = tripService.getTripBalances(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip balances calculated successfully", response));
    }
}
