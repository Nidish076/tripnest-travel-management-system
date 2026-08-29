package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.LoginRequest;
import com.tripnest.backend.dto.request.RegisterRequest;
import com.tripnest.backend.dto.response.AuthResponse;
import com.tripnest.backend.dto.response.UserSummaryResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserSummaryResponse getCurrentUser();
}
