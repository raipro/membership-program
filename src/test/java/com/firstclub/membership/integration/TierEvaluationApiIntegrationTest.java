package com.firstclub.membership.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for tier evaluation opening (or keeping closed) the eligibility gate.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TierEvaluationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void premiumCohort_fastTracksToGold_thenSubscribeGoldSucceeds() throws Exception {
        // User 2 is PREMIUM → cohort criterion qualifies them for Gold.
        mockMvc.perform(post("/api/v1/users/2/tier/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleTierCode").value("GOLD"));

        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":2,"planId":2,"tierId":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tierCode").value("GOLD"));
    }

    @Test
    void monthlySpend_unlocksPlatinum_thenUpgradeSucceeds() throws Exception {
        // Three 5000 orders → 15000 spend clears Platinum's 10000 threshold.
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/users/1/orders").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":5000}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/v1/users/1/tier/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleTierCode").value("PLATINUM"))
                .andExpect(jsonPath("$.totalSpend").value(15000.00));

        String created = mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":1,"planId":1,"tierId":1}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long subId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/v1/subscriptions/{id}/upgrade", subId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tierId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tierCode").value("PLATINUM"))
                .andExpect(jsonPath("$.price").value(399.00));
    }

    @Test
    void noActivity_staysBaseTier_andUpgradeIsBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/users/3/tier/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleTierCode").value("SILVER"));

        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":3,"planId":1,"tierId":1}
                                """))
                .andExpect(status().isCreated());

        // Still gated for Gold (last op — poisons the tx).
        mockMvc.perform(post("/api/v1/subscriptions/{id}/upgrade", lastSubscriptionId(3))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tierId\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("not unlocked")));
    }

    private long lastSubscriptionId(long userId) throws Exception {
        String json = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/v1/users/{id}/subscription", userId))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }
}
