package com.tripnest.backend.config;

import com.tripnest.backend.model.User;
import com.tripnest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@tripnest.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("TripNest Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .role("ROLE_ADMIN")
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("Initialized default administrator: {} / Admin@123", adminEmail);
        }
    }
}
