package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.TravelPreferenceRequest;
import com.tripnest.backend.dto.response.TravelPreferenceResponse;
import com.tripnest.backend.entity.TravelPreferences;
import com.tripnest.backend.entity.User;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.TravelPreferenceRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.TravelPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TravelPreferenceServiceImpl implements TravelPreferenceService {

    private final TravelPreferenceRepository travelPreferenceRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public TravelPreferenceResponse getPreferences() {
        User user = getAuthenticatedUser();
        TravelPreferences preferences = travelPreferenceRepository.findByUser(user)
                .orElseGet(() -> {
                    TravelPreferences defaultPrefs = TravelPreferences.builder()
                            .user(user)
                            .preferredDestinations(new ArrayList<>())
                            .preferredActivities(new ArrayList<>())
                            .build();
                    return travelPreferenceRepository.save(defaultPrefs);
                });

        return mapToResponse(preferences);
    }

    @Override
    @Transactional
    public TravelPreferenceResponse updatePreferences(TravelPreferenceRequest request) {
        User user = getAuthenticatedUser();
        TravelPreferences preferences = travelPreferenceRepository.findByUser(user)
                .orElseGet(() -> TravelPreferences.builder().user(user).build());

        if (request.getPreferredTravelType() != null) {
            preferences.setPreferredTravelType(request.getPreferredTravelType());
        }
        if (request.getPreferredDestinations() != null) {
            preferences.setPreferredDestinations(new ArrayList<>(request.getPreferredDestinations()));
        }
        if (request.getBudgetRange() != null) {
            preferences.setBudgetRange(request.getBudgetRange());
        }
        if (request.getPreferredActivities() != null) {
            preferences.setPreferredActivities(new ArrayList<>(request.getPreferredActivities()));
        }
        if (request.getPreferredTransportation() != null) {
            preferences.setPreferredTransportation(request.getPreferredTransportation());
        }
        if (request.getPreferredAccommodationType() != null) {
            preferences.setPreferredAccommodationType(request.getPreferredAccommodationType());
        }

        TravelPreferences saved = travelPreferenceRepository.save(preferences);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TravelPreferenceResponse getPreferencesByUserId(Long userId) {
        TravelPreferences preferences = travelPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPreferences", "userId", userId));

        return mapToResponse(preferences);
    }

    private TravelPreferenceResponse mapToResponse(TravelPreferences preferences) {
        return TravelPreferenceResponse.builder()
                .id(preferences.getId())
                .userId(preferences.getUser() != null ? preferences.getUser().getId() : null)
                .preferredTravelType(preferences.getPreferredTravelType())
                .preferredDestinations(preferences.getPreferredDestinations() != null ? new ArrayList<>(preferences.getPreferredDestinations()) : new ArrayList<>())
                .budgetRange(preferences.getBudgetRange())
                .preferredActivities(preferences.getPreferredActivities() != null ? new ArrayList<>(preferences.getPreferredActivities()) : new ArrayList<>())
                .preferredTransportation(preferences.getPreferredTransportation())
                .preferredAccommodationType(preferences.getPreferredAccommodationType())
                .createdAt(preferences.getCreatedAt())
                .updatedAt(preferences.getUpdatedAt())
                .build();
    }
}
