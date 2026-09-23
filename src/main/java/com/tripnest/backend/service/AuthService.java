package com.tripnest.backend.service;

import com.tripnest.backend.dto.AuthResponse;
import com.tripnest.backend.dto.LoginRequest;
import com.tripnest.backend.dto.RegisterRequest;
import com.tripnest.backend.dto.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser();
}
