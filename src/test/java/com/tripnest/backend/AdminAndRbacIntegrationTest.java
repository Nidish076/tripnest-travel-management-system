package com.tripnest.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripnest.backend.dto.request.DestinationRequest;
import com.tripnest.backend.dto.request.LoginRequest;
import com.tripnest.backend.dto.request.RegisterRequest;
import com.tripnest.backend.dto.request.UserStatusRequest;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminAndRbacIntegrationTest {

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

    private String obtainToken(String email, String password) throws Exception {
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
    public void testAdminCapabilitiesAndRbacAccessControl() throws Exception {
        // 1. Obtain Admin Token
        String adminToken = obtainToken("admin@tripnest.com", "Admin@123");
        assertNotNull(adminToken);

        // 2. Create a test regular user
        String regularUserEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Normal User")
                .email(regularUserEmail)
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String userToken = obtainToken(regularUserEmail, "Password@123");
        assertNotNull(userToken);

        // 3. Verify RBAC: Regular user CANNOT access Admin endpoints -> 403 Forbidden
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 4. Verify unauthenticated request -> 401 Unauthorized
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        // 5. Admin can list users
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());

        // 6. Admin can search and filter users
        mockMvc.perform(get("/api/admin/users")
                        .param("query", "Normal User")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value(regularUserEmail));

        // 7. Admin can view user detail
        Long regularUserId = userRepository.findByEmail(regularUserEmail).get().getId();
        mockMvc.perform(get("/api/admin/users/" + regularUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(regularUserId))
                .andExpect(jsonPath("$.data.profile").exists())
                .andExpect(jsonPath("$.data.travelPreferences").exists())
                .andExpect(jsonPath("$.data.accountSettings").exists());

        // 8. Admin can update user status (deactivate)
        UserStatusRequest deactivateReq = UserStatusRequest.builder().isActive(false).build();
        mockMvc.perform(put("/api/admin/users/" + regularUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));

        // 9. Admin can manage destinations: Create destination
        DestinationRequest newDest = DestinationRequest.builder()
                .name("Machu Picchu Sanctuary " + System.currentTimeMillis())
                .country("Peru")
                .city("Cusco")
                .description("Ancient Incan citadel set high in the Andes Mountains.")
                .imageUrl("https://images.unsplash.com/photo-1526392060635-9d6019884377")
                .bestTimeToVisit("May - October")
                .category("Historical")
                .isPopular(true)
                .build();

        MvcResult createDestResult = mockMvc.perform(post("/api/admin/destinations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.country").value("Peru"))
                .andReturn();

        String createJson = createDestResult.getResponse().getContentAsString();
        Map<?, ?> createdMap = objectMapper.readValue(createJson, Map.class);
        Map<?, ?> createdData = (Map<?, ?>) createdMap.get("data");
        Number createdIdNum = (Number) createdData.get("id");
        Long createdDestId = createdIdNum.longValue();

        // 10. Admin can update destination
        newDest.setDescription("Updated description for Machu Picchu Incan citadel.");
        mockMvc.perform(put("/api/admin/destinations/" + createdDestId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated description for Machu Picchu Incan citadel."));

        // 11. Admin can view popular destinations analytics
        mockMvc.perform(get("/api/admin/destinations/popular")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 12. Admin can delete destination
        mockMvc.perform(delete("/api/admin/destinations/" + createdDestId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
