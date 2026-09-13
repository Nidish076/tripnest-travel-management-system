package com.tripnest.backend.controller;

import com.tripnest.backend.dto.response.ApiResponse;
import com.tripnest.backend.dto.response.AttractionResponse;
import com.tripnest.backend.dto.response.DestinationResponse;
import com.tripnest.backend.service.AttractionService;
import com.tripnest.backend.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;
    private final AttractionService attractionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DestinationResponse>>> searchDestinations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isPopular,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        Page<DestinationResponse> destinations = destinationService.searchDestinations(
                query, country, category, isPopular, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("Destinations retrieved successfully", destinations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DestinationResponse>> getDestinationById(@PathVariable Long id) {
        DestinationResponse destination = destinationService.getDestinationById(id);
        return ResponseEntity.ok(ApiResponse.success("Destination retrieved successfully", destination));
    }

    @GetMapping("/{id}/attractions")
    public ResponseEntity<ApiResponse<List<AttractionResponse>>> getAttractionsByDestinationId(@PathVariable Long id) {
        List<AttractionResponse> attractions = attractionService.getAttractionsByDestinationId(id);
        return ResponseEntity.ok(ApiResponse.success("Attractions retrieved successfully", attractions));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<DestinationResponse>>> getPopularDestinations() {
        List<DestinationResponse> popular = destinationService.getPopularDestinations();
        return ResponseEntity.ok(ApiResponse.success("Popular destinations retrieved successfully", popular));
    }
}
