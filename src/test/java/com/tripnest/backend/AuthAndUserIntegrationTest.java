package com.tripnest.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripnest.backend.dto.request.*;
import com.tripnest.backend.entity.Destination;
import com.tripnest.backend.repository.DestinationRepository;
import com.tripnest.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthAndUserIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseJson, Map.class);
        Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
        return (String) dataMap.get("token");
    }

    @Test
    public void testUserRegistrationAndLifecycle() throws Exception {
        String uniqueEmail = "traveler_" + System.currentTimeMillis() + "@example.com";

        // 1. Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Alex Wanderer")
                .email(uniqueEmail)
                .password("Password@123")
                .phone("+1-555-0100")
                .age(28)
                .location("Seattle, WA")
                .bio("Avid globetrotter and nature photographer.")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(uniqueEmail))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_USER"))
                .andReturn();

        // 2. Login with newly registered user
        String token = obtainAccessToken(uniqueEmail, "Password@123");
        assertNotNull(token);

        // 3. View Profile
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alex Wanderer"))
                .andExpect(jsonPath("$.data.email").value(uniqueEmail))
                .andExpect(jsonPath("$.data.age").value(28))
                .andExpect(jsonPath("$.data.location").value("Seattle, WA"));

        // 4. Update Profile
        ProfileRequest updateProfileReq = ProfileRequest.builder()
                .name("Alexander Wanderer")
                .email(uniqueEmail)
                .phone("+1-555-9999")
                .age(29)
                .location("Vancouver, Canada")
                .bio("Updated travel bio.")
                .build();

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alexander Wanderer"))
                .andExpect(jsonPath("$.data.location").value("Vancouver, Canada"))
                .andExpect(jsonPath("$.data.age").value(29));

        // 5. Update and View Travel Preferences
        TravelPreferenceRequest prefRequest = TravelPreferenceRequest.builder()
                .preferredTravelType("Solo")
                .preferredDestinations(Arrays.asList("Japan", "Switzerland", "New Zealand"))
                .budgetRange("Moderate")
                .preferredActivities(Arrays.asList("Adventure", "Nature", "Beaches"))
                .preferredTransportation("Flight")
                .preferredAccommodationType("Hostel")
                .build();

        mockMvc.perform(put("/api/users/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredTravelType").value("Solo"))
                .andExpect(jsonPath("$.data.budgetRange").value("Moderate"))
                .andExpect(jsonPath("$.data.preferredAccommodationType").value("Hostel"));

        mockMvc.perform(get("/api/users/preferences")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredTravelType").value("Solo"))
                .andExpect(jsonPath("$.data.preferredTransportation").value("Flight"));

        // 6. Favorites Management
        List<Destination> destinations = destinationRepository.findAll();
        assertFalse(destinations.isEmpty());
        Long destId = destinations.get(0).getId();

        // Add favorite
        mockMvc.perform(post("/api/users/favorites/" + destId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.destination.id").value(destId));

        // View favorites
        mockMvc.perform(get("/api/users/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].destination.id").value(destId));

        // Remove favorite
        mockMvc.perform(delete("/api/users/favorites/" + destId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify empty favorites
        mockMvc.perform(get("/api/users/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        // 7. Update Account Settings
        AccountSettingRequest settingsReq = AccountSettingRequest.builder()
                .emailNotifications(false)
                .pushNotifications(true)
                .promoEmails(true)
                .build();

        mockMvc.perform(put("/api/users/settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailNotifications").value(false))
                .andExpect(jsonPath("$.data.promoEmails").value(true));

        // 8. Change Password
        ChangePasswordRequest pwReq = ChangePasswordRequest.builder()
                .currentPassword("Password@123")
                .newPassword("NewSecret@999")
                .build();

        mockMvc.perform(put("/api/users/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pwReq)))
                .andExpect(status().isOk());

        // Verify new password works for login
        String newToken = obtainAccessToken(uniqueEmail, "NewSecret@999");
        assertNotNull(newToken);
    }
}
