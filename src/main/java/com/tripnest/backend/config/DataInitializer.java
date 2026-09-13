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
            Destination santorini = Destination.builder()
                    .name("Santorini Caldera & Oia")
                    .city("Santorini")
                    .country("Greece")
                    .category("Beaches")
                    .description("Iconic whitewashed buildings perched on rugged volcanic cliffs overlooking the cobalt Aegean Sea.")
                    .imageUrl("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff")
                    .bestTimeToVisit("May - October")
                    .isPopular(true)
                    .favoriteCount(124L)
                    .currency("EUR")
                    .language("Greek, English")
                    .climate("Mediterranean with warm dry summers and mild winters")
                    .transportation("Ferries, rental ATVs/scooters, and local KTEL buses")
                    .visaRequirements("Schengen Visa required for non-EU travelers")
                    .timeZone("EET (UTC+2)")
                    .travelTips(Arrays.asList(
                            "Book sunset viewing spots in Oia well in advance",
                            "Wear comfortable shoes for cobblestone paths and steps",
                            "Try local Assyrtiko white wine and fava meletia"
                    ))
                    .build();
            santorini.addAttraction(Attraction.builder()
                    .name("Oia Sunset Viewpoint")
                    .description("World-famous panoramic viewpoint offering breathtaking sunsets over the whitewashed cliffside and Aegean Sea.")
                    .imageUrl("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff")
                    .location("Oia, Santorini")
                    .category("Sightseeing")
                    .entryFee(0.0)
                    .build());
            santorini.addAttraction(Attraction.builder()
                    .name("Red Beach (Kokkini Paralia)")
                    .description("Unique volcanic beach known for its towering rust-red cliffs and dark pebble sands.")
                    .imageUrl("https://images.unsplash.com/photo-1533105079780-92b9be482077")
                    .location("Akrotiri, Santorini")
                    .category("Nature")
                    .entryFee(0.0)
                    .build());
            santorini.addAttraction(Attraction.builder()
                    .name("Ancient Akrotiri Archaeological Site")
                    .description("Minoan Bronze Age settlement preserved under volcanic ash, often called the Pompeii of the Aegean.")
                    .imageUrl("https://images.unsplash.com/photo-1516483638261-f4dbaf036963")
                    .location("Akrotiri, Santorini")
                    .category("Historical")
                    .entryFee(12.0)
                    .build());

            Destination kyoto = Destination.builder()
                    .name("Kyoto Arashiyama & Gion")
                    .city("Kyoto")
                    .country("Japan")
                    .category("Cultural")
                    .description("Historic temples, serene bamboo groves, traditional tea houses, and breathtaking cherry blossom gardens.")
                    .imageUrl("https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e")
                    .bestTimeToVisit("March - May & October - November")
                    .isPopular(true)
                    .favoriteCount(98L)
                    .currency("JPY")
                    .language("Japanese, English")
                    .climate("Humid subtropical with distinct hot summers and chilly winters")
                    .transportation("Kyoto City Bus, Hankyu & Keihan Railway, IC cards (Suica/Pasmo)")
                    .visaRequirements("Visa-free for 90 days for many passport holders, tourist visa otherwise")
                    .timeZone("JST (UTC+9)")
                    .travelTips(Arrays.asList(
                            "Visit popular shrines early in the morning before crowds arrive",
                            "Purchase an IC transit card for seamless train and bus travel",
                            "Respect Geisha etiquette in Gion; do not take photos without permission"
                    ))
                    .build();
            kyoto.addAttraction(Attraction.builder()
                    .name("Fushimi Inari Taisha")
                    .description("Iconic Shinto shrine famous for thousands of vibrant vermilion torii gates winding up Mount Inari.")
                    .imageUrl("https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e")
                    .location("Fushimi-ku, Kyoto")
                    .category("Cultural")
                    .entryFee(0.0)
                    .build());
            kyoto.addAttraction(Attraction.builder()
                    .name("Kinkaku-ji (Golden Pavilion)")
                    .description("Zen Buddhist temple with the top two floors covered in dazzling gold leaf, set in a mirror pond.")
                    .imageUrl("https://images.unsplash.com/photo-1503899036084-c55cdd92da26")
                    .location("Kita-ku, Kyoto")
                    .category("Historical")
                    .entryFee(500.0)
                    .build());
            kyoto.addAttraction(Attraction.builder()
                    .name("Arashiyama Bamboo Grove")
                    .description("Enchanting path surrounded by soaring green bamboo stalks that rustle gently in the mountain breeze.")
                    .imageUrl("https://images.unsplash.com/photo-1545569341-9eb8b30979d9")
                    .location("Arashiyama, Kyoto")
                    .category("Nature")
                    .entryFee(0.0)
                    .build());

            Destination banff = Destination.builder()
                    .name("Banff National Park & Lake Louise")
                    .city("Banff")
                    .country("Canada")
                    .category("Nature")
                    .description("Turquoise glacial lakes framed by towering Rocky Mountain peaks and pristine pine forests.")
                    .imageUrl("https://images.unsplash.com/photo-1503614472-8c93d56e92ce")
                    .bestTimeToVisit("June - September & December - March")
                    .isPopular(true)
                    .favoriteCount(87L)
                    .currency("CAD")
                    .language("English, French")
                    .climate("Subarctic alpine with snowy winters and cool, sunny summers")
                    .transportation("Roam Transit, personal/rental car, Parks Canada shuttles")
                    .visaRequirements("eTA required for visa-exempt travelers, visitor visa for others")
                    .timeZone("MST (UTC-7)")
                    .travelTips(Arrays.asList(
                            "Parks Canada Discovery Pass is required for park entry",
                            "Book Lake Louise and Moraine Lake shuttles months in advance",
                            "Always carry bear spray and make noise when hiking"
                    ))
                    .build();
            banff.addAttraction(Attraction.builder()
                    .name("Lake Louise")
                    .description("Glacial lake famed for sparkling turquoise water framed by Mount Victoria and Victoria Glacier.")
                    .imageUrl("https://images.unsplash.com/photo-1503614472-8c93d56e92ce")
                    .location("Lake Louise, AB")
                    .category("Nature")
                    .entryFee(0.0)
                    .build());
            banff.addAttraction(Attraction.builder()
                    .name("Moraine Lake & Valley of the Ten Peaks")
                    .description("Stunning glacier-fed lake surrounded by ten towering peaks in the Valley of the Ten Peaks.")
                    .imageUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb")
                    .location("Banff National Park, AB")
                    .category("Nature")
                    .entryFee(0.0)
                    .build());
            banff.addAttraction(Attraction.builder()
                    .name("Banff Gondola at Sulphur Mountain")
                    .description("Scenic gondola ride carrying visitors to the summit of Sulphur Mountain for 360-degree Rocky Mountain views.")
                    .imageUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b")
                    .location("Banff, AB")
                    .category("Adventure")
                    .entryFee(65.0)
                    .build());

            Destination amalfi = Destination.builder()
                    .name("Amalfi Coast & Positano")
                    .city("Amalfi")
                    .country("Italy")
                    .category("Beaches")
                    .description("Dramatic coastal cliffs, pastel-colored villages cascading into the Mediterranean, and vibrant lemon groves.")
                    .imageUrl("https://images.unsplash.com/photo-1533105079780-92b9be482077")
                    .bestTimeToVisit("April - October")
                    .isPopular(true)
                    .favoriteCount(76L)
                    .currency("EUR")
                    .language("Italian, English")
                    .climate("Mediterranean with hot summers and mild rainy winters")
                    .transportation("SITA buses, passenger ferries, scooter rentals")
                    .visaRequirements("Schengen Visa required for non-EU travelers")
                    .timeZone("CET (UTC+1)")
                    .travelTips(Arrays.asList(
                            "Take ferries between coastal towns to bypass narrow winding cliffside traffic",
                            "Wear comfortable walking shoes with grip for steep staircases",
                            "Pack sea sickness medication if you are sensitive on coastal ferries or buses"
                    ))
                    .build();
            amalfi.addAttraction(Attraction.builder()
                    .name("Positano Spiaggia Grande")
                    .description("The central beach of Positano, surrounded by colorful cliffside villas, restaurants, and crystal-clear waters.")
                    .imageUrl("https://images.unsplash.com/photo-1533105079780-92b9be482077")
                    .location("Positano, SA")
                    .category("Beaches")
                    .entryFee(0.0)
                    .build());
            amalfi.addAttraction(Attraction.builder()
                    .name("Amalfi Cathedral (Duomo di Amalfi)")
                    .description("Stately 9th-century cathedral in Piazza del Duomo featuring Romanesque and Moorish architecture.")
                    .imageUrl("https://images.unsplash.com/photo-1516483638261-f4dbaf036963")
                    .location("Amalfi, SA")
                    .category("Historical")
                    .entryFee(3.0)
                    .build());
            amalfi.addAttraction(Attraction.builder()
                    .name("Path of the Gods (Sentiero degli Dei)")
                    .description("Spectacular hiking trail along cliff ridges high above the sea connecting Bomerano to Nocelle.")
                    .imageUrl("https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
                    .location("Agerola to Positano")
                    .category("Adventure")
                    .entryFee(0.0)
                    .build());

            Destination swissAlps = Destination.builder()
                    .name("Swiss Alps & Zermatt")
                    .city("Zermatt")
                    .country("Switzerland")
                    .category("Mountains")
                    .description("World-class alpine skiing, hiking trails, and majestic vistas of the legendary Matterhorn.")
                    .imageUrl("https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99")
                    .bestTimeToVisit("December - April & July - September")
                    .isPopular(true)
                    .favoriteCount(65L)
                    .currency("CHF")
                    .language("German, French, English")
                    .climate("Alpine with cold snowy winters and temperate alpine summers")
                    .transportation("Matterhorn Gotthard Bahn, Gornergrat cog railway, electric e-taxis (Zermatt is car-free)")
                    .visaRequirements("Schengen Visa required for non-EU travelers")
                    .timeZone("CET (UTC+1)")
                    .travelTips(Arrays.asList(
                            "Zermatt is strictly car-free; park in Tasch and take the shuttle train",
                            "Get a Swiss Travel Pass or Peak Pass for unlimited mountain excursions",
                            "Dress in layers as alpine temperatures change rapidly"
                    ))
                    .build();
            swissAlps.addAttraction(Attraction.builder()
                    .name("Matterhorn Glacier Paradise")
                    .description("Highest cable car station in Europe at 3,883m, with year-round snow, ice palace, and viewing platform.")
                    .imageUrl("https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99")
                    .location("Zermatt, Valais")
                    .category("Adventure")
                    .entryFee(95.0)
                    .build());
            swissAlps.addAttraction(Attraction.builder()
                    .name("Gornergrat Cogwheel Railway & Viewpoint")
                    .description("Historic open-air cog railway ascending to 3,089m with stunning vistas of 29 four-thousand-meter peaks.")
                    .imageUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b")
                    .location("Zermatt, Valais")
                    .category("Sightseeing")
                    .entryFee(88.0)
                    .build());
            swissAlps.addAttraction(Attraction.builder()
                    .name("Sunnegga & Five Lakes Walk")
                    .description("Iconic alpine hiking circuit visiting five pristine mountain lakes reflecting the Matterhorn.")
                    .imageUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb")
                    .location("Zermatt, Valais")
                    .category("Nature")
                    .entryFee(0.0)
                    .build());

            List<Destination> starterDestinations = Arrays.asList(santorini, kyoto, banff, amalfi, swissAlps);
            destinationRepository.saveAll(starterDestinations);
            log.info("Initialized {} starter travel destinations with travel information and attractions", starterDestinations.size());
        }
    }
}
