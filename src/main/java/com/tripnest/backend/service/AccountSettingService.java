package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.AccountSettingRequest;
import com.tripnest.backend.dto.request.ChangePasswordRequest;
import com.tripnest.backend.dto.request.UpdateContactRequest;
import com.tripnest.backend.dto.response.AccountSettingResponse;
import com.tripnest.backend.dto.response.ProfileResponse;

public interface AccountSettingService {
    AccountSettingResponse getSettings();
    AccountSettingResponse updateSettings(AccountSettingRequest request);
    void changePassword(ChangePasswordRequest request);
    ProfileResponse updateContact(UpdateContactRequest request);
    void disableAccount();
    void deleteAccount();
    AccountSettingResponse getSettingsByUserId(Long userId);
}
