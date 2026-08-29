package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.AccountSettingRequest;
import com.tripnest.backend.dto.request.ChangePasswordRequest;
import com.tripnest.backend.dto.request.UpdateContactRequest;
import com.tripnest.backend.dto.response.AccountSettingResponse;
import com.tripnest.backend.dto.response.ProfileResponse;
import com.tripnest.backend.entity.AccountSettings;
import com.tripnest.backend.entity.Profile;
import com.tripnest.backend.entity.User;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.AccountSettingRepository;
import com.tripnest.backend.repository.ProfileRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.AccountSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AccountSettingServiceImpl implements AccountSettingService {

    private final AccountSettingRepository accountSettingRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    private User getAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountSettingResponse getSettings() {
        User user = getAuthenticatedUser();
        AccountSettings settings = accountSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    AccountSettings defaultSettings = AccountSettings.builder()
                            .user(user)
                            .emailNotifications(true)
                            .pushNotifications(true)
                            .promoEmails(false)
                            .isAccountActive(true)
                            .build();
                    return accountSettingRepository.save(defaultSettings);
                });

        return mapToResponse(settings);
    }

    @Override
    @Transactional
    public AccountSettingResponse updateSettings(AccountSettingRequest request) {
        User user = getAuthenticatedUser();
        AccountSettings settings = accountSettingRepository.findByUser(user)
                .orElseGet(() -> AccountSettings.builder().user(user).build());

        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPushNotifications() != null) {
            settings.setPushNotifications(request.getPushNotifications());
        }
        if (request.getPromoEmails() != null) {
            settings.setPromoEmails(request.getPromoEmails());
        }
        if (request.getIsAccountActive() != null) {
            settings.setIsAccountActive(request.getIsAccountActive());
            user.setIsActive(request.getIsAccountActive());
            userRepository.save(user);
        }

        AccountSettings saved = accountSettingRepository.save(settings);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateContact(UpdateContactRequest request) {
        User user = getAuthenticatedUser();
        Profile profile = profileRepository.findByUser(user)
                .orElseGet(() -> Profile.builder().user(user).name(user.getName()).email(user.getEmail()).build());

        if (StringUtils.hasText(request.getEmail())) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new BadRequestException("Email is already registered: " + newEmail);
            }
            user.setEmail(newEmail);
            profile.setEmail(newEmail);
        }

        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }

        userRepository.save(user);
        Profile savedProfile = profileRepository.save(profile);

        return ProfileResponse.builder()
                .id(savedProfile.getId())
                .userId(user.getId())
                .name(savedProfile.getName())
                .email(savedProfile.getEmail())
                .phone(savedProfile.getPhone())
                .profilePhoto(savedProfile.getProfilePhoto())
                .age(savedProfile.getAge())
                .location(savedProfile.getLocation())
                .bio(savedProfile.getBio())
                .createdAt(savedProfile.getCreatedAt())
                .updatedAt(savedProfile.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void disableAccount() {
        User user = getAuthenticatedUser();
        user.setIsActive(false);
        userRepository.save(user);

        AccountSettings settings = accountSettingRepository.findByUser(user).orElse(null);
        if (settings != null) {
            settings.setIsAccountActive(false);
            accountSettingRepository.save(settings);
        }
    }

    @Override
    @Transactional
    public void deleteAccount() {
        User user = getAuthenticatedUser();
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountSettingResponse getSettingsByUserId(Long userId) {
        AccountSettings settings = accountSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AccountSettings", "userId", userId));

        return mapToResponse(settings);
    }

    private AccountSettingResponse mapToResponse(AccountSettings settings) {
        return AccountSettingResponse.builder()
                .id(settings.getId())
                .userId(settings.getUser() != null ? settings.getUser().getId() : null)
                .emailNotifications(settings.getEmailNotifications())
                .pushNotifications(settings.getPushNotifications())
                .promoEmails(settings.getPromoEmails())
                .isAccountActive(settings.getIsAccountActive())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
