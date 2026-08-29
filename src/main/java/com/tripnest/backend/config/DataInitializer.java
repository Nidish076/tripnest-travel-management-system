package com.tripnest.backend.config;

import com.tripnest.backend.entity.*;
import com.tripnest.backend.repository.DestinationRepository;
import com.tripnest.backend.repository.RoleRepository;
import com.tripnest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
        seedSampleDestinations();
    }

    private void seedRoles() {
        if (!roleRepository.existsByRoleName(Role.RoleName.ROLE_USER)) {
            roleRepository.save(Role.builder().roleName(Role.RoleName.ROLE_USER).build());
            log.info("Initialized ROLE_USER");
        }
        if (!roleRepository.existsByRoleName(Role.RoleName.ROLE_ADMIN)) {
            roleRepository.save(Role.builder().roleName(Role.RoleName.ROLE_ADMIN).build());
            log.info("Initialized ROLE_ADMIN");
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@tripnest.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByRoleName(Role.RoleName.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName(Role.RoleName.ROLE_ADMIN).build()));

            User admin = User.builder()
                    .name("TripNest Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(adminRole)
                    .isActive(true)
                    .build();

            Profile adminProfile = Profile.builder()
                    .name("TripNest Administrator")
                    .email(adminEmail)
                    .phone("+1-800-555-0199")
                    .location("San Francisco, CA")
                    .bio("Head of Operations and Global Travel Curation at TripNest.")
                    .build();
            admin.setProfile(adminProfile);

            TravelPreferences adminPrefs = TravelPreferences.builder()
                    .preferredTravelType("Solo")
                    .budgetRange("Luxury")
                    .preferredActivities(Arrays.asList("Adventure", "Historical", "Cultural"))
                    .preferredTransportation("Flight")
                    .preferredAccommodationType("Resort")
                    .build();
            admin.setTravelPreferences(adminPrefs);

            AccountSettings adminSettings = AccountSettings.builder()
                    .emailNotifications(true)
                    .pushNotifications(true)
                    .promoEmails(false)
                    .isAccountActive(true)
                    .build();
            admin.setAccountSettings(adminSettings);

            userRepository.save(admin);
            log.info("Initialized default administrator: {} / Admin@123", adminEmail);
        }
    }

    private void seedSampleDestinations() {
        if (destinationRepository.count() == 0) {
            List<Destination> starterDestinations = Arrays.asList(
                    Destination.builder()
                            .name("Santorini Caldera & Oia")
                            .city("Santorini")
                            .country("Greece")
                            .category("Beaches")
                            .description("Iconic whitewashed buildings perched on rugged volcanic cliffs overlooking the cobalt Aegean Sea.")
                            .imageUrl("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff")
                            .bestTimeToVisit("May - October")
                            .isPopular(true)
                            .favoriteCount(124L)
                            .build(),
                    Destination.builder()
                            .name("Kyoto Arashiyama & Gion")
                            .city("Kyoto")
                            .country("Japan")
                            .category("Cultural")
                            .description("Historic temples, serene bamboo groves, traditional tea houses, and breathtaking cherry blossom gardens.")
                            .imageUrl("https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e")
                            .bestTimeToVisit("March - May & October - November")
                            .isPopular(true)
                            .favoriteCount(98L)
                            .build(),
                    Destination.builder()
                            .name("Banff National Park & Lake Louise")
                            .city("Banff")
                            .country("Canada")
                            .category("Nature")
                            .description("Turquoise glacial lakes framed by towering Rocky Mountain peaks and pristine pine forests.")
                            .imageUrl("https://images.unsplash.com/photo-1503614472-8c93d56e92ce")
                            .bestTimeToVisit("June - September & December - March")
                            .isPopular(true)
                            .favoriteCount(87L)
                            .build(),
                    Destination.builder()
                            .name("Amalfi Coast & Positano")
                            .city("Amalfi")
                            .country("Italy")
                            .category("Beaches")
                            .description("Dramatic coastal cliffs, pastel-colored villages cascading into the Mediterranean, and vibrant lemon groves.")
                            .imageUrl("https://images.unsplash.com/photo-1533105079780-92b9be482077")
                            .bestTimeToVisit("April - October")
                            .isPopular(true)
                            .favoriteCount(76L)
                            .build(),
                    Destination.builder()
                            .name("Swiss Alps & Zermatt")
                            .city("Zermatt")
                            .country("Switzerland")
                            .category("Mountains")
                            .description("World-class alpine skiing, hiking trails, and majestic vistas of the legendary Matterhorn.")
                            .imageUrl("https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99")
                            .bestTimeToVisit("December - April & July - September")
                            .isPopular(true)
                            .favoriteCount(65L)
                            .build()
            );

            destinationRepository.saveAll(starterDestinations);
            log.info("Initialized {} starter travel destinations", starterDestinations.size());
        }
    }
}
