package com.tripnest.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripnest.backend.dto.request.AttractionRequest;
import com.tripnest.backend.dto.request.DestinationRequest;
import com.tripnest.backend.dto.request.LoginRequest;
import com.tripnest.backend.dto.request.RegisterRequest;
import com.tripnest.backend.entity.Attraction;
import com.tripnest.backend.entity.Destination;
import com.tripnest.backend.repository.AttractionRepository;
import com.tripnest.backend.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DestinationAndAttractionsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private AttractionRepository attractionRepository;

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

    private String registerAndGetToken(String prefix) throws Exception {
        String email = prefix + "_" + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Test User " + prefix)
                .email(email)
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        return obtainToken(email, "Password@123");
    }

    @Test
    @DisplayName("TEST 1: Public destination details returns destination data and travel information")
    public void test1_PublicDestinationDetails_ReturnsDataAndTravelInformation() throws Exception {
        List<Destination> destinations = destinationRepository.findAll();
        assertTrue(!destinations.isEmpty(), "Destinations must exist");
        Long destId = destinations.get(0).getId();

        mockMvc.perform(get("/api/destinations/" + destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(destId))
                .andExpect(jsonPath("$.data.name").isNotEmpty())
                .andExpect(jsonPath("$.data.currency").isNotEmpty())
                .andExpect(jsonPath("$.data.language").isNotEmpty())
                .andExpect(jsonPath("$.data.climate").isNotEmpty())
                .andExpect(jsonPath("$.data.transportation").isNotEmpty())
                .andExpect(jsonPath("$.data.visaRequirements").isNotEmpty())
                .andExpect(jsonPath("$.data.timeZone").isNotEmpty())
                .andExpect(jsonPath("$.data.travelTips").isArray())
                .andExpect(jsonPath("$.data.travelTips", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.attractions").isArray())
                .andExpect(jsonPath("$.data.attractions", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("TEST 2: Public attraction retrieval returns HTTP 200")
    public void test2_PublicAttractionRetrieval_ReturnsHttp200() throws Exception {
        List<Destination> destinations = destinationRepository.findAll();
        Long destId = destinations.get(0).getId();

        mockMvc.perform(get("/api/destinations/" + destId + "/attractions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].name").isNotEmpty())
                .andExpect(jsonPath("$.data[0].category").isNotEmpty());
    }

    @Test
    @DisplayName("TEST 3: Attractions for invalid destination returns HTTP 404")
    public void test3_AttractionsForInvalidDestination_ReturnsHttp404() throws Exception {
        mockMvc.perform(get("/api/destinations/999999/attractions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("TEST 4: Admin can add attraction with admin token and returns successful response (HTTP 201)")
    public void test4_AdminCanAddAttraction_ReturnsSuccess() throws Exception {
        String adminToken = obtainToken("admin@tripnest.com", "Admin@123");
        assertNotNull(adminToken);

        List<Destination> destinations = destinationRepository.findAll();
        Long destId = destinations.get(0).getId();

        AttractionRequest createRequest = AttractionRequest.builder()
                .name("Admin Added Overlook")
                .description("Scenic panoramic cliff overlook created by admin.")
                .imageUrl("https://example.com/overlook.jpg")
                .location("North Overlook")
                .category("Sightseeing")
                .entryFee(12.50)
                .build();

        mockMvc.perform(post("/api/admin/destinations/" + destId + "/attractions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Admin Added Overlook"))
                .andExpect(jsonPath("$.data.entryFee").value(12.50))
                .andExpect(jsonPath("$.data.destinationId").value(destId));
    }

    @Test
    @DisplayName("TEST 5: Regular user cannot add attraction with ROLE_USER and returns HTTP 403")
    public void test5_RegularUserCannotAddAttraction_ReturnsHttp403() throws Exception {
        String regularUserToken = registerAndGetToken("user_add_attraction");
        List<Destination> destinations = destinationRepository.findAll();
        Long destId = destinations.get(0).getId();

        AttractionRequest request = AttractionRequest.builder()
                .name("Unauthorized Overlook")
                .description("Should be rejected with 403")
                .build();

        mockMvc.perform(post("/api/admin/destinations/" + destId + "/attractions")
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TEST 6: Admin can update attraction and returns successful response (HTTP 200)")
    public void test6_AdminCanUpdateAttraction_ReturnsSuccess() throws Exception {
        String adminToken = obtainToken("admin@tripnest.com", "Admin@123");
        List<Attraction> attractions = attractionRepository.findAll();
        assertTrue(!attractions.isEmpty(), "Attractions must exist");
        Long attractionId = attractions.get(0).getId();

        AttractionRequest updateRequest = AttractionRequest.builder()
                .name("Updated Attraction Name " + System.currentTimeMillis())
                .description("Updated high quality description.")
                .imageUrl("https://example.com/updated.jpg")
                .location("Updated Location")
                .category("Adventure")
                .entryFee(30.0)
                .build();

        mockMvc.perform(put("/api/admin/attractions/" + attractionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.data.location").value("Updated Location"))
                .andExpect(jsonPath("$.data.category").value("Adventure"))
                .andExpect(jsonPath("$.data.entryFee").value(30.0));
    }

    @Test
    @DisplayName("TEST 7: Admin can delete attraction and returns successful response")
    public void test7_AdminCanDeleteAttraction_ReturnsSuccess() throws Exception {
        String adminToken = obtainToken("admin@tripnest.com", "Admin@123");
        List<Destination> destinations = destinationRepository.findAll();
        Long destId = destinations.get(0).getId();

        // 1. First create an attraction specifically to delete
        AttractionRequest tempRequest = AttractionRequest.builder()
                .name("Temporary Attraction to Delete")
                .description("Will be deleted by test 7")
                .entryFee(5.0)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/admin/destinations/" + destId + "/attractions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tempRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createJson = createResult.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(createJson, Map.class);
        Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
        Long tempAttractionId = ((Number) dataMap.get("id")).longValue();

        // 2. Admin deletes the attraction
        mockMvc.perform(delete("/api/admin/attractions/" + tempAttractionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Verify attraction is removed from repository
        assertTrue(!attractionRepository.findById(tempAttractionId).isPresent());
    }

    @Test
    @DisplayName("TEST 8: Non-admin cannot update or delete attractions and returns HTTP 403")
    public void test8_NonAdminCannotUpdateOrDeleteAttractions_ReturnsHttp403() throws Exception {
        String regularUserToken = registerAndGetToken("user_modify_attraction");
        List<Attraction> attractions = attractionRepository.findAll();
        Long attractionId = attractions.get(0).getId();

        AttractionRequest request = AttractionRequest.builder()
                .name("Malicious Update")
                .description("Should be forbidden")
                .build();

        // Regular user PUT -> 403
        mockMvc.perform(put("/api/admin/attractions/" + attractionId)
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Regular user DELETE -> 403
        mockMvc.perform(delete("/api/admin/attractions/" + attractionId)
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());

        // Anonymous PUT -> 401
        mockMvc.perform(put("/api/admin/attractions/" + attractionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        // Anonymous DELETE -> 401
        mockMvc.perform(delete("/api/admin/attractions/" + attractionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Validation: Blank name or negative entryFee fails validation with 400 Bad Request")
    public void testAttractionValidationFailsForInvalidInputs() throws Exception {
        String adminToken = obtainToken("admin@tripnest.com", "Admin@123");
        List<Destination> destinations = destinationRepository.findAll();
        Long destId = destinations.get(0).getId();

        // Blank name
        AttractionRequest blankNameRequest = AttractionRequest.builder()
                .name("")
                .description("Valid description")
                .build();

        mockMvc.perform(post("/api/admin/destinations/" + destId + "/attractions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankNameRequest)))
                .andExpect(status().isBadRequest());

        // Negative entry fee
        AttractionRequest negativeFeeRequest = AttractionRequest.builder()
                .name("Valid Name")
                .entryFee(-10.0)
                .build();

        mockMvc.perform(post("/api/admin/destinations/" + destId + "/attractions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativeFeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Admin CRUD: Admin can create destination with comprehensive travel information")
    public void testAdminCanCreateDestinationWithTravelInformation() throws Exception {
        String adminToken = obtainToken("admin@tripnest.com", "Admin@123");

        DestinationRequest destRequest = DestinationRequest.builder()
                .name("Reykjavik & Golden Circle " + System.currentTimeMillis())
                .city("Reykjavik")
                .country("Iceland")
                .category("Nature")
                .description("Land of fire and ice featuring geysers, hot springs, glaciers, and waterfalls.")
                .imageUrl("https://example.com/iceland.jpg")
                .bestTimeToVisit("June - August & September - March")
                .currency("ISK")
                .language("Icelandic, English")
                .climate("Subpolar oceanic with cool summers and relatively mild winters")
                .transportation("Rental 4x4 vehicles, tour buses, Reykjavik city bus")
                .visaRequirements("Schengen Visa required for non-EU travelers")
                .timeZone("GMT (UTC+0)")
                .travelTips(Arrays.asList(
                        "Rent a 4WD vehicle if exploring beyond Ring Road",
                        "Pre-book Blue Lagoon admission weeks ahead",
                        "Always monitor local weather and road conditions"
                ))
                .build();

        MvcResult result = mockMvc.perform(post("/api/admin/destinations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(destRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currency").value("ISK"))
                .andExpect(jsonPath("$.data.language").value("Icelandic, English"))
                .andExpect(jsonPath("$.data.travelTips", hasSize(3)))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<?, ?> map = objectMapper.readValue(json, Map.class);
        Map<?, ?> data = (Map<?, ?>) map.get("data");
        Long newDestId = ((Number) data.get("id")).longValue();

        // Also verify public GET on the newly created destination
        mockMvc.perform(get("/api/destinations/" + newDestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currency").value("ISK"))
                .andExpect(jsonPath("$.data.timeZone").value("GMT (UTC+0)"))
                .andExpect(jsonPath("$.data.travelTips", hasSize(3)));
    }
}
