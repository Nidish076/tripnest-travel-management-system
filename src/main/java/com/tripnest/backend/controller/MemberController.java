package com.tripnest.backend.controller;

import com.tripnest.backend.dto.ApiResponse;
import com.tripnest.backend.dto.JoinTripRequest;
import com.tripnest.backend.dto.TripMemberResponse;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<TripMemberResponse>> joinTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody JoinTripRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        TripMemberResponse response = memberService.joinTrip(tripId, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Successfully joined trip", response));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<TripMemberResponse>>> getTripMembers(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        List<TripMemberResponse> response = memberService.getTripMembers(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip members retrieved successfully", response));
    }

    @DeleteMapping("/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long tripId,
            @PathVariable Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        memberService.removeMember(tripId, userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }
}
