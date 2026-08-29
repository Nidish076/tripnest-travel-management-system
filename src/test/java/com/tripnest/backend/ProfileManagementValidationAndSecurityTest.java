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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProfileManagementValidationAndSecurityTest {

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

    private String registerAndObtainToken(String name, String email, String password) throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        return obtainAccessToken(email, password);
    }

    @Test
    public void testUnauthenticatedAccessToProtectedEndpoints() throws Exception {
        // Access profile endpoints without token
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProfileRequest.builder().name("Test").build())))
                .andExpect(status().isUnauthorized());

        // Access preference endpoints without token
        mockMvc.perform(get("/api/users/preferences"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TravelPreferenceRequest.builder().build())))
                .andExpect(status().isUnauthorized());

        // Access favorites endpoints without token
        mockMvc.perform(get("/api/users/favorites"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/users/favorites/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/favorites/1"))
                .andExpect(status().isUnauthorized());

        // Access settings endpoints without token
        mockMvc.perform(get("/api/users/settings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AccountSettingRequest.builder().build())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChangePasswordRequest.builder().build())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateContactRequest.builder().build())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/deactivate"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/account"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUserDataIsolation() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userAEmail = "usera_" + timestamp + "@example.com";
        String userBEmail = "userb_" + timestamp + "@example.com";

        // Register A and B
        String tokenA = registerAndObtainToken("User A", userAEmail, "Password@123");
        String tokenB = registerAndObtainToken("User B", userBEmail, "Password@123");

        // 1. Profile Isolation
        // User A updates profile
        ProfileRequest profileA = ProfileRequest.builder()
                .name("User A Updated")
                .email(userAEmail)
                .age(30)
                .location("New York")
                .bio("Bio of User A")
                .build();
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileA)))
                .andExpect(status().isOk());

        // User B views profile. Should see User B details, NOT A's details
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("User B"))
                .andExpect(jsonPath("$.data.email").value(userBEmail))
                .andExpect(jsonPath("$.data.location").isEmpty());

        // 2. Travel Preferences Isolation
        // User A updates preferences
        TravelPreferenceRequest prefsA = TravelPreferenceRequest.builder()
                .preferredTravelType("Solo")
                .budgetRange("Luxury")
                .preferredDestinations(Collections.singletonList("Paris"))
                .build();
        mockMvc.perform(put("/api/users/preferences")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefsA)))
                .andExpect(status().isOk());

        // User B gets preferences. Should get defaults, not User A's
        mockMvc.perform(get("/api/users/preferences")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredTravelType").isEmpty())
                .andExpect(jsonPath("$.data.budgetRange").isEmpty());

        // 3. Account Settings Isolation
        // User A updates settings
        AccountSettingRequest settingsA = AccountSettingRequest.builder()
                .emailNotifications(false)
                .pushNotifications(false)
                .build();
        mockMvc.perform(put("/api/users/settings")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingsA)))
                .andExpect(status().isOk());

        // User B gets settings. Should get defaults (true), not User A's
        mockMvc.perform(get("/api/users/settings")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailNotifications").value(true))
                .andExpect(jsonPath("$.data.pushNotifications").value(true));

        // 4. Favorites Isolation
        List<Destination> destinations = destinationRepository.findAll();
        assertFalse(destinations.isEmpty(), "Destinations should not be empty");
        Long destId = destinations.get(0).getId();

        // User A adds to favorites
        mockMvc.perform(post("/api/users/favorites/" + destId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        // User B lists favorites. Should be empty
        mockMvc.perform(get("/api/users/favorites")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testProfileRequestValidationConstraints() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userEmail = "validation_" + timestamp + "@example.com";
        String token = registerAndObtainToken("Validation User", userEmail, "Password@123");

        // 1. Blank Name
        ProfileRequest blankNameReq = ProfileRequest.builder()
                .name("   ")
                .email(userEmail)
                .build();
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankNameReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.name").value("Name is required"));

        // 2. Name too short
        ProfileRequest shortNameReq = ProfileRequest.builder()
                .name("A")
                .email(userEmail)
                .build();
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortNameReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.name").value("Name must be between 2 and 100 characters"));

        // 3. Name too long
        char[] chars = new char[101];
        Arrays.fill(chars, 'a');
        String longName = new String(chars);
        ProfileRequest longNameReq = ProfileRequest.builder()
                .name(longName)
                .email(userEmail)
                .build();
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longNameReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.name").value("Name must be between 2 and 100 characters"));

        // 4. Invalid Email Format
        ProfileRequest invalidEmailReq = ProfileRequest.builder()
                .name("Valid Name")
                .email("not-a-valid-email")
                .build();
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").value("Email must be a valid email address"));
    }

    @Test
    public void testDuplicateEmailUpdates() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userAEmail = "usera_dup_" + timestamp + "@example.com";
        String userBEmail = "userb_dup_" + timestamp + "@example.com";

        // Register User A and User B
        String tokenA = registerAndObtainToken("User A", userAEmail, "Password@123");
        registerAndObtainToken("User B", userBEmail, "Password@123");

        // User A tries to update profile email to User B's email
        ProfileRequest duplicateProfileReq = ProfileRequest.builder()
                .name("User A")
                .email(userBEmail)
                .build();
        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateProfileReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already in use: " + userBEmail));

        // User A tries to update contact email to User B's email
        UpdateContactRequest duplicateContactReq = UpdateContactRequest.builder()
                .email(userBEmail)
                .build();
        mockMvc.perform(put("/api/users/contact")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateContactReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already registered: " + userBEmail));
    }

    @Test
    public void testChangePasswordEdgeCases() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userEmail = "password_" + timestamp + "@example.com";
        String token = registerAndObtainToken("Password User", userEmail, "Password@123");

        // 1. Wrong current password
        ChangePasswordRequest wrongCurrentPwReq = ChangePasswordRequest.builder()
                .currentPassword("WrongPassword123")
                .newPassword("NewPassword@123")
                .build();
        mockMvc.perform(put("/api/users/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongCurrentPwReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));

        // 2. New password same as current password
        ChangePasswordRequest samePwReq = ChangePasswordRequest.builder()
                .currentPassword("Password@123")
                .newPassword("Password@123")
                .build();
        mockMvc.perform(put("/api/users/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samePwReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("New password cannot be the same as the current password"));

        // 3. New password too short
        ChangePasswordRequest shortPwReq = ChangePasswordRequest.builder()
                .currentPassword("Password@123")
                .newPassword("Short")
                .build();
        mockMvc.perform(put("/api/users/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortPwReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.newPassword").value("New password must be at least 6 characters"));
    }

    @Test
    public void testFavoritesManagementEdgeCases() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userEmail = "favorites_" + timestamp + "@example.com";
        String token = registerAndObtainToken("Favorites User", userEmail, "Password@123");

        List<Destination> destinations = destinationRepository.findAll();
        assertFalse(destinations.isEmpty(), "Destinations should not be empty");
        Long destId = destinations.get(0).getId();

        // 1. Add non-existent destination
        mockMvc.perform(post("/api/users/favorites/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // 2. Add destination successfully first
        mockMvc.perform(post("/api/users/favorites/" + destId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // 3. Add same destination again (should throw duplicate)
        mockMvc.perform(post("/api/users/favorites/" + destId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Destination is already in your favorites"));

        // 4. Remove non-existent destination
        mockMvc.perform(delete("/api/users/favorites/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // 5. Remove destination that belongs to another user (or not favorited by this user)
        // Let's find another destination not favorited by this user
        Long otherDestId = null;
        for (Destination d : destinations) {
            if (!d.getId().equals(destId)) {
                otherDestId = d.getId();
                break;
            }
        }
        assertNotNull(otherDestId, "Should have more than 1 destination for this test");

        mockMvc.perform(delete("/api/users/favorites/" + otherDestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Favorite destination not found in your list"));
    }

    @Test
    public void testAccountDeactivationAndDeletionSecurity() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userEmail = "deactivate_" + timestamp + "@example.com";
        String token = registerAndObtainToken("Deactivate User", userEmail, "Password@123");

        // Verify we can access profile successfully
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 1. Deactivate account
        mockMvc.perform(put("/api/users/deactivate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 2. Try to log in. Should fail.
        LoginRequest loginRequest = LoginRequest.builder()
                .email(userEmail)
                .password("Password@123")
                .build();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        // Create a new user to test account deletion
        String deleteEmail = "delete_" + timestamp + "@example.com";
        String deleteToken = registerAndObtainToken("Delete User", deleteEmail, "Password@123");

        // Delete account
        mockMvc.perform(delete("/api/users/account")
                        .header("Authorization", "Bearer " + deleteToken))
                .andExpect(status().isOk());

        // Try to log in with deleted account. Should fail.
        LoginRequest deleteLoginRequest = LoginRequest.builder()
                .email(deleteEmail)
                .password("Password@123")
                .build();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteLoginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
