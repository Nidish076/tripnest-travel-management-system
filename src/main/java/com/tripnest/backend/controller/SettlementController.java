package com.tripnest.backend.controller;

import com.tripnest.backend.dto.ApiResponse;
import com.tripnest.backend.dto.SettlementRequest;
import com.tripnest.backend.dto.SettlementResponse;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/api/trips/{tripId}/settlements")
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(
            @PathVariable Long tripId,
            @Valid @RequestBody SettlementRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        SettlementResponse response = settlementService.createSettlement(tripId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement recorded successfully", response));
    }

    @GetMapping("/api/trips/{tripId}/settlements")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getTripSettlements(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        List<SettlementResponse> response = settlementService.getTripSettlements(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip settlements retrieved successfully", response));
    }

    @PutMapping("/api/settlements/{id}/complete")
    public ResponseEntity<ApiResponse<SettlementResponse>> completeSettlement(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        SettlementResponse response = settlementService.completeSettlement(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Settlement completed successfully", response));
    }
}
