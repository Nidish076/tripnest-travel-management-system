package com.tripnest.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripnest.backend.dto.*;
import com.tripnest.backend.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TripNestIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String registerAndGetToken(String name, String email, String password) throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseJson, Map.class);
        Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
        return (String) dataMap.get("token");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseJson, Map.class);
        Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
        return (String) dataMap.get("token");
    }

    @Test
    public void testCompleteTripNestLifecycle() throws Exception {
        long timestamp = System.currentTimeMillis();
        String email1 = "nidish_" + timestamp + "@example.com";
        String email2 = "rahul_" + timestamp + "@example.com";
        String email3 = "priya_" + timestamp + "@example.com";
        String outsiderEmail = "outsider_" + timestamp + "@example.com";

        // 1. Register User 1 (Nidish) & Login
        String token1 = registerAndGetToken("Nidish", email1, "password123");
        assertNotNull(token1);

        String loginToken1 = loginAndGetToken(email1, "password123");
        assertNotNull(loginToken1);

        // Verify /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nidish"))
                .andExpect(jsonPath("$.data.email").value(email1));

        // 2. Create Trip (Goa Trip)
        CreateTripRequest tripRequest = CreateTripRequest.builder()
                .name("Goa Trip")
                .destination("Goa")
                .description("College friends trip")
                .startDate(LocalDate.of(2026, 12, 10))
                .endDate(LocalDate.of(2026, 12, 14))
                .budget(30000.0)
                .build();

        MvcResult tripResult = mockMvc.perform(post("/api/trips")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Goa Trip"))
                .andExpect(jsonPath("$.data.destination").value("Goa"))
                .andExpect(jsonPath("$.data.inviteCode").isNotEmpty())
                .andExpect(jsonPath("$.data.userRole").value("OWNER"))
                .andReturn();

        String tripJson = tripResult.getResponse().getContentAsString();
        Map<?, ?> tripMap = objectMapper.readValue(tripJson, Map.class);
        Map<?, ?> tripData = (Map<?, ?>) tripMap.get("data");
        Long tripId = ((Number) tripData.get("id")).longValue();
        String inviteCode = (String) tripData.get("inviteCode");

        // 3. Register User 2 (Rahul) and User 3 (Priya)
        String token2 = registerAndGetToken("Rahul", email2, "password123");
        String token3 = registerAndGetToken("Priya", email3, "password123");

        // 4. Rahul joins trip using invite code
        JoinTripRequest joinReq = JoinTripRequest.builder().inviteCode(inviteCode).build();
        mockMvc.perform(post("/api/trips/" + tripId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.name").value("Rahul"));

        // Duplicate join should be rejected
        mockMvc.perform(post("/api/trips/" + tripId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Priya joins trip
        mockMvc.perform(post("/api/trips/" + tripId + "/join")
                        .header("Authorization", "Bearer " + token3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MEMBER"));

        // 5. Verify trip members
        MvcResult membersResult = mockMvc.perform(get("/api/trips/" + tripId + "/members")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andReturn();

        String membersJson = membersResult.getResponse().getContentAsString();
        Map<?, ?> membersMap = objectMapper.readValue(membersJson, Map.class);
        List<?> membersList = (List<?>) membersMap.get("data");
        Map<?, ?> u1 = (Map<?, ?>) membersList.get(0);
        Map<?, ?> u2 = (Map<?, ?>) membersList.get(1);
        Map<?, ?> u3 = (Map<?, ?>) membersList.get(2);
        Long user1Id = ((Number) u1.get("userId")).longValue();
        Long user2Id = ((Number) u2.get("userId")).longValue();
        Long user3Id = ((Number) u3.get("userId")).longValue();

        // 6. Itinerary Management
        ItineraryRequest itinReq1 = ItineraryRequest.builder()
                .title("Visit Baga Beach")
                .description("Beach and lunch")
                .location("Baga Beach, Goa")
                .date(LocalDate.of(2026, 12, 11))
                .startTime("10:00")
                .endTime("14:00")
                .estimatedCost(1500.0)
                .build();

        MvcResult itinResult = mockMvc.perform(post("/api/trips/" + tripId + "/itinerary")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itinReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Visit Baga Beach"))
                .andExpect(jsonPath("$.data.date").value("2026-12-11"))
                .andReturn();

        String itinJson = itinResult.getResponse().getContentAsString();
        Map<?, ?> itinMap = objectMapper.readValue(itinJson, Map.class);
        Long itinId = ((Number) ((Map<?, ?>) itinMap.get("data")).get("id")).longValue();

        // Query itinerary by date
        mockMvc.perform(get("/api/trips/" + tripId + "/itinerary/2026-12-11")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Visit Baga Beach"));

        // Update itinerary item
        itinReq1.setTitle("Visit Baga Beach & Water Sports");
        mockMvc.perform(put("/api/itinerary/" + itinId)
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itinReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Visit Baga Beach & Water Sports"));

        // 7. Shared Expenses & Split Validation
        // Test split validation failure: sum(splits) != amount
        ExpenseRequest invalidExpense = ExpenseRequest.builder()
                .title("Invalid Hotel")
                .amount(6000.0)
                .category(Expense.ExpenseCategory.HOTEL)
                .description("Two rooms")
                .expenseDate(LocalDate.of(2026, 12, 10))
                .paidBy(user1Id)
                .splits(List.of(
                        ExpenseSplitRequest.builder().userId(user1Id).amount(2000.0).build(),
                        ExpenseSplitRequest.builder().userId(user2Id).amount(2000.0).build()
                        // Missing User 3's 2000, so sum is 4000 != 6000
                ))
                .build();

        mockMvc.perform(post("/api/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidExpense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Valid Expense 1: Hotel 6000 split equally among all 3 members (2000 each) paid by User 1 (Nidish)
        ExpenseRequest validExpense1 = ExpenseRequest.builder()
                .title("Hotel")
                .amount(6000.0)
                .category(Expense.ExpenseCategory.HOTEL)
                .description("Two rooms")
                .expenseDate(LocalDate.of(2026, 12, 10))
                .paidBy(user1Id)
                .splits(List.of(
                        ExpenseSplitRequest.builder().userId(user1Id).amount(2000.0).build(),
                        ExpenseSplitRequest.builder().userId(user2Id).amount(2000.0).build(),
                        ExpenseSplitRequest.builder().userId(user3Id).amount(2000.0).build()
                ))
                .build();

        mockMvc.perform(post("/api/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validExpense1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Hotel"))
                .andExpect(jsonPath("$.data.splits.length()").value(3));

        // Valid Expense 2: Food 1500 paid by User 2 (Rahul), equal split among all members (no explicit splits array)
        ExpenseRequest validExpense2 = ExpenseRequest.builder()
                .title("Dinner")
                .amount(1500.0)
                .category(Expense.ExpenseCategory.FOOD)
                .description("Seafood dinner")
                .expenseDate(LocalDate.of(2026, 12, 11))
                .paidBy(user2Id)
                .build();

        mockMvc.perform(post("/api/trips/" + tripId + "/expenses")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validExpense2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Dinner"))
                .andExpect(jsonPath("$.data.splits.length()").value(3));

        // 8. Automatic Balance Calculation & Debt Simplification
        MvcResult balancesResult = mockMvc.perform(get("/api/trips/" + tripId + "/balances")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalExpenses").value(7500.0))
                .andExpect(jsonPath("$.data.members.length()").value(3))
                .andExpect(jsonPath("$.data.settlementSuggestions").isArray())
                .andReturn();

        String balancesJson = balancesResult.getResponse().getContentAsString();
        Map<?, ?> balancesMap = objectMapper.readValue(balancesJson, Map.class);
        Map<?, ?> balancesData = (Map<?, ?>) balancesMap.get("data");
        List<?> suggestions = (List<?>) balancesData.get("settlementSuggestions");
        assertFalse(suggestions.isEmpty(), "Settlement suggestions should be calculated");

        // 9. Settlements
        // Create Settlement: Rahul pays Nidish 1000
        SettlementRequest settlementReq = SettlementRequest.builder()
                .payerId(user2Id)
                .receiverId(user1Id)
                .amount(1000.0)
                .build();

        MvcResult settlementResult = mockMvc.perform(post("/api/trips/" + tripId + "/settlements")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settlementReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.amount").value(1000.0))
                .andReturn();

        String settleJson = settlementResult.getResponse().getContentAsString();
        Map<?, ?> settleMap = objectMapper.readValue(settleJson, Map.class);
        Long settleId = ((Number) ((Map<?, ?>) settleMap.get("data")).get("id")).longValue();

        // Complete Settlement
        mockMvc.perform(put("/api/settlements/" + settleId + "/complete")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.settledAt").isNotEmpty());

        // 10. Access Control & Security Verification
        // Non-member (Outsider) cannot view trip details
        String outsiderToken = registerAndGetToken("Outsider", outsiderEmail, "password123");
        mockMvc.perform(get("/api/trips/" + tripId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());

        // 11. Member leaving / removal
        // User 3 (Priya) leaves trip
        mockMvc.perform(delete("/api/trips/" + tripId + "/members/" + user3Id)
                        .header("Authorization", "Bearer " + token3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // OWNER trying to leave should be rejected
        mockMvc.perform(delete("/api/trips/" + tripId + "/members/" + user1Id)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isBadRequest());

        // Delete itinerary item
        mockMvc.perform(delete("/api/itinerary/" + itinId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Delete Trip by OWNER
        mockMvc.perform(delete("/api/trips/" + tripId)
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
