package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.TravelPreferenceRequest;
import com.tripnest.backend.dto.response.TravelPreferenceResponse;

public interface TravelPreferenceService {
    TravelPreferenceResponse getPreferences();
    TravelPreferenceResponse updatePreferences(TravelPreferenceRequest request);
    TravelPreferenceResponse getPreferencesByUserId(Long userId);
}
