package com.tripnest.backend.controller;

import com.tripnest.backend.dto.request.*;
import com.tripnest.backend.dto.response.*;
import com.tripnest.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;
    private final TravelPreferenceService travelPreferenceService;
    private final FavoriteDestinationService favoriteDestinationService;
    private final AccountSettingService accountSettingService;

    // --- Profile Endpoints ---

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.createOrUpdateProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully", response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        ProfileResponse response = profileService.getProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    // --- Travel Preferences Endpoints ---

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<TravelPreferenceResponse>> getPreferences() {
        TravelPreferenceResponse response = travelPreferenceService.getPreferences();
        return ResponseEntity.ok(ApiResponse.success("Travel preferences retrieved successfully", response));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<TravelPreferenceResponse>> updatePreferences(@RequestBody TravelPreferenceRequest request) {
        TravelPreferenceResponse response = travelPreferenceService.updatePreferences(request);
        return ResponseEntity.ok(ApiResponse.success("Travel preferences updated successfully", response));
    }

    // --- Favorite Destinations Endpoints ---

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteDestinationResponse>>> getFavorites() {
        List<FavoriteDestinationResponse> response = favoriteDestinationService.getUserFavorites();
        return ResponseEntity.ok(ApiResponse.success("Favorite destinations retrieved successfully", response));
    }

    @PostMapping("/favorites/{destinationId}")
    public ResponseEntity<ApiResponse<FavoriteDestinationResponse>> addFavorite(@PathVariable Long destinationId) {
        FavoriteDestinationResponse response = favoriteDestinationService.addFavorite(destinationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Destination added to favorites successfully", response));
    }

    @DeleteMapping("/favorites/{destinationId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(@PathVariable Long destinationId) {
        favoriteDestinationService.removeFavorite(destinationId);
        return ResponseEntity.ok(ApiResponse.success("Destination removed from favorites successfully", null));
    }

    // --- Account Settings Endpoints ---

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<AccountSettingResponse>> getSettings() {
        AccountSettingResponse response = accountSettingService.getSettings();
        return ResponseEntity.ok(ApiResponse.success("Account settings retrieved successfully", response));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<AccountSettingResponse>> updateSettings(@RequestBody AccountSettingRequest request) {
        AccountSettingResponse response = accountSettingService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Account settings updated successfully", response));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountSettingService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PutMapping("/contact")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateContact(@Valid @RequestBody UpdateContactRequest request) {
        ProfileResponse response = accountSettingService.updateContact(request);
        return ResponseEntity.ok(ApiResponse.success("Contact details updated successfully", response));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount() {
        accountSettingService.disableAccount();
        return ResponseEntity.ok(ApiResponse.success("Account deactivated successfully", null));
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<String>> deleteAccount() {
        accountSettingService.deleteAccount();
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
