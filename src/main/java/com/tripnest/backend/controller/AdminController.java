package com.tripnest.backend.controller;

import com.tripnest.backend.dto.request.AttractionRequest;
import com.tripnest.backend.dto.request.DestinationRequest;
import com.tripnest.backend.dto.request.UserStatusRequest;
import com.tripnest.backend.dto.response.AdminUserDetailResponse;
import com.tripnest.backend.dto.response.ApiResponse;
import com.tripnest.backend.dto.response.AttractionResponse;
import com.tripnest.backend.dto.response.DestinationResponse;
import com.tripnest.backend.dto.response.UserSummaryResponse;
import com.tripnest.backend.service.AdminService;
import com.tripnest.backend.service.AttractionService;
import com.tripnest.backend.service.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DestinationService destinationService;
    private final AttractionService attractionService;

    // --- User Management Endpoints ---

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserSummaryResponse>>> getAllUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Page<UserSummaryResponse> users = adminService.searchUsers(query, isActive, role, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserById(@PathVariable Long id) {
        AdminUserDetailResponse user = adminService.getUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", user));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {

        UserSummaryResponse response = adminService.updateUserStatus(id, request.getIsActive());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // --- Destination Management Endpoints ---

    @PostMapping("/destinations")
    public ResponseEntity<ApiResponse<DestinationResponse>> createDestination(@Valid @RequestBody DestinationRequest request) {
        DestinationResponse response = destinationService.createDestination(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Destination created successfully", response));
    }

    @PutMapping("/destinations/{id}")
    public ResponseEntity<ApiResponse<DestinationResponse>> updateDestination(
            @PathVariable Long id,
            @Valid @RequestBody DestinationRequest request) {

        DestinationResponse response = destinationService.updateDestination(id, request);
        return ResponseEntity.ok(ApiResponse.success("Destination updated successfully", response));
    }

    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDestination(@PathVariable Long id) {
        destinationService.deleteDestination(id);
        return ResponseEntity.ok(ApiResponse.success("Destination deleted successfully", null));
    }

    @GetMapping("/destinations/popular")
    public ResponseEntity<ApiResponse<List<DestinationResponse>>> getPopularDestinationsAnalytics() {
        List<DestinationResponse> destinations = adminService.getPopularDestinationsAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Popular destinations analytics retrieved successfully", destinations));
    }

    // --- Attraction Management Endpoints ---

    @PostMapping("/destinations/{id}/attractions")
    public ResponseEntity<ApiResponse<AttractionResponse>> createAttraction(
            @PathVariable Long id,
            @Valid @RequestBody AttractionRequest request) {
        AttractionResponse response = attractionService.addAttraction(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attraction created successfully", response));
    }

    @PutMapping("/attractions/{id}")
    public ResponseEntity<ApiResponse<AttractionResponse>> updateAttraction(
            @PathVariable Long id,
            @Valid @RequestBody AttractionRequest request) {
        AttractionResponse response = attractionService.updateAttraction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attraction updated successfully", response));
    }

    @DeleteMapping("/attractions/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAttraction(@PathVariable Long id) {
        attractionService.deleteAttraction(id);
        return ResponseEntity.ok(ApiResponse.success("Attraction deleted successfully", null));
    }
}
