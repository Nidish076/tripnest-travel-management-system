package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.response.*;
import com.tripnest.backend.entity.*;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.repository.*;
import com.tripnest.backend.service.AdminService;
import com.tripnest.backend.service.FavoriteDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TravelPreferenceRepository travelPreferenceRepository;
    private final AccountSettingRepository accountSettingRepository;
    private final FavoriteDestinationRepository favoriteDestinationRepository;
    private final FavoriteDestinationService favoriteDestinationService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> searchUsers(String query, Boolean isActive, String role, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Role.RoleName roleName = null;
        if (role != null && !role.isBlank()) {
            try {
                roleName = Role.RoleName.valueOf(role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<User> users = userRepository.searchUsers(
                (query != null && !query.isBlank()) ? query.trim() : null,
                isActive,
                roleName,
                pageable
        );

        return users.map(user -> UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getRoleName().name() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Profile profile = profileRepository.findByUser(user).orElse(null);
        ProfileResponse profileResponse = profile != null ? ProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .name(profile.getName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .profilePhoto(profile.getProfilePhoto())
                .age(profile.getAge())
                .location(profile.getLocation())
                .bio(profile.getBio())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build() : null;

        TravelPreferences prefs = travelPreferenceRepository.findByUser(user).orElse(null);
        TravelPreferenceResponse prefResponse = prefs != null ? TravelPreferenceResponse.builder()
                .id(prefs.getId())
                .userId(user.getId())
                .preferredTravelType(prefs.getPreferredTravelType())
                .preferredDestinations(prefs.getPreferredDestinations() != null ? new ArrayList<>(prefs.getPreferredDestinations()) : new ArrayList<>())
                .budgetRange(prefs.getBudgetRange())
                .preferredActivities(prefs.getPreferredActivities() != null ? new ArrayList<>(prefs.getPreferredActivities()) : new ArrayList<>())
                .preferredTransportation(prefs.getPreferredTransportation())
                .preferredAccommodationType(prefs.getPreferredAccommodationType())
                .createdAt(prefs.getCreatedAt())
                .updatedAt(prefs.getUpdatedAt())
                .build() : null;

        AccountSettings settings = accountSettingRepository.findByUser(user).orElse(null);
        AccountSettingResponse settingsResponse = settings != null ? AccountSettingResponse.builder()
                .id(settings.getId())
                .userId(user.getId())
                .emailNotifications(settings.getEmailNotifications())
                .pushNotifications(settings.getPushNotifications())
                .promoEmails(settings.getPromoEmails())
                .isAccountActive(settings.getIsAccountActive())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build() : null;

        List<FavoriteDestination> favorites = favoriteDestinationRepository.findByUserOrderByCreatedAtDesc(user);

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getRoleName().name() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .profile(profileResponse)
                .travelPreferences(prefResponse)
                .accountSettings(settingsResponse)
                .favoriteDestinationsCount(favorites != null ? favorites.size() : 0)
                .build();
    }

    @Override
    @Transactional
    public UserSummaryResponse updateUserStatus(Long id, Boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(isActive);
        User saved = userRepository.save(user);

        AccountSettings settings = accountSettingRepository.findByUser(user).orElse(null);
        if (settings != null) {
            settings.setIsAccountActive(isActive);
            accountSettingRepository.save(settings);
        }

        return UserSummaryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole() != null ? saved.getRole().getRoleName().name() : null)
                .isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationResponse> getPopularDestinationsAnalytics() {
        return favoriteDestinationService.getMostFavoritedDestinations();
    }
}
