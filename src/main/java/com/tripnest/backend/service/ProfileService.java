package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.ProfileRequest;
import com.tripnest.backend.dto.response.ProfileResponse;

public interface ProfileService {
    ProfileResponse getProfile();
    ProfileResponse updateProfile(ProfileRequest request);
    ProfileResponse createOrUpdateProfile(ProfileRequest request);
    ProfileResponse getProfileByUserId(Long userId);
}
