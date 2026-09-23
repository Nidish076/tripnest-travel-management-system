package com.tripnest.backend.controller;

import com.tripnest.backend.dto.ApiResponse;
import com.tripnest.backend.dto.ItineraryRequest;
import com.tripnest.backend.dto.ItineraryResponse;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping("/api/trips/{tripId}/itinerary")
    public ResponseEntity<ApiResponse<ItineraryResponse>> addItineraryItem(
            @PathVariable Long tripId,
            @Valid @RequestBody ItineraryRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        ItineraryResponse response = itineraryService.addItineraryItem(tripId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Itinerary item added successfully", response));
    }

    @GetMapping("/api/trips/{tripId}/itinerary")
    public ResponseEntity<ApiResponse<List<ItineraryResponse>>> getItinerary(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        List<ItineraryResponse> response = itineraryService.getItinerary(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Itinerary items retrieved successfully", response));
    }

    @GetMapping("/api/trips/{tripId}/itinerary/{date}")
    public ResponseEntity<ApiResponse<List<ItineraryResponse>>> getItineraryByDate(
            @PathVariable Long tripId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        List<ItineraryResponse> response = itineraryService.getItineraryByDate(tripId, date, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Itinerary items for " + date + " retrieved successfully", response));
    }

    @PutMapping("/api/itinerary/{id}")
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateItineraryItem(
            @PathVariable Long id,
            @Valid @RequestBody ItineraryRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        ItineraryResponse response = itineraryService.updateItineraryItem(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Itinerary item updated successfully", response));
    }

    @DeleteMapping("/api/itinerary/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItineraryItem(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        itineraryService.deleteItineraryItem(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Itinerary item deleted successfully"));
    }
}
