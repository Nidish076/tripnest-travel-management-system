package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.ProfileRequest;
import com.tripnest.backend.dto.response.ProfileResponse;
import com.tripnest.backend.entity.Profile;
import com.tripnest.backend.entity.User;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.ProfileRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        User user = getAuthenticatedUser();
        Profile profile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    Profile defaultProfile = Profile.builder()
                            .user(user)
                            .name(user.getName())
                            .email(user.getEmail())
                            .build();
                    return profileRepository.save(defaultProfile);
                });

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(ProfileRequest request) {
        User user = getAuthenticatedUser();
        Profile profile = profileRepository.findByUser(user)
                .orElseGet(() -> Profile.builder().user(user).build());

        if (StringUtils.hasText(request.getName())) {
            profile.setName(request.getName().trim());
            user.setName(request.getName().trim());
        }

        if (StringUtils.hasText(request.getEmail())) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new BadRequestException("Email is already in use: " + newEmail);
            }
            profile.setEmail(newEmail);
            user.setEmail(newEmail);
        }

        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getProfilePhoto() != null) {
            profile.setProfilePhoto(request.getProfilePhoto());
        }
        if (request.getAge() != null) {
            profile.setAge(request.getAge());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        userRepository.save(user);
        Profile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile);
    }

    @Override
    @Transactional
    public ProfileResponse createOrUpdateProfile(ProfileRequest request) {
        return updateProfile(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        return mapToResponse(profile);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .name(profile.getName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .profilePhoto(profile.getProfilePhoto())
                .age(profile.getAge())
                .location(profile.getLocation())
                .bio(profile.getBio())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
