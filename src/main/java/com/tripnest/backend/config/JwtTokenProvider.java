package com.tripnest.backend.config;

import com.tripnest.backend.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtService jwtService;

    public JwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String generateToken(Authentication authentication) {
        return jwtService.generateToken(authentication);
    }

    public String generateTokenFromUser(Long userId, String email, String name, String roleName) {
        return jwtService.generateToken(userId, email, name, roleName);
    }

    public String getEmailFromJwt(String token) {
        return jwtService.getEmailFromJwt(token);
    }

    public boolean validateToken(String authToken) {
        return jwtService.validateToken(authToken);
    }
}
