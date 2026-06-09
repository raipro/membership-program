package com.firstclub.membership.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for the subscribe / cancel flow and the eligibility gate, using
 * users 1-3 (which have no seeded subscriptions). Error-expecting requests are last
 * in each method so the rollback-only test transaction is never read again.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubscriptionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String subscribe(long userId, long planId, long tierId) {
        return """
                {"userId":%d,"planId":%d,"tierId":%d}
                """.formatted(userId, planId, tierId);
    }

    @Test
    void subscribeToBaseTier_succeeds_andIsTrackable() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(subscribe(1, 1, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tierCode").value("SILVER"))
                .andExpect(jsonPath("$.price").value(99.00))
                .andExpect(jsonPath("$.daysRemaining").value(30));

        mockMvc.perform(get("/api/v1/users/1/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tierCode").value("SILVER"));
    }

    @Test
    void cancelFreesTheSlot_allowingReSubscribe() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(subscribe(1, 1, 1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/subscriptions/{id}/cancel", lastSubscriptionId(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Slot released → a fresh subscription is allowed.
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(subscribe(1, 2, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planCode").value("QUARTERLY"));
    }

    @Test
    void idempotentRetry_returnsSameSubscription() throws Exception {
        String body = """
                {"userId":3,"planId":1,"tierId":1,"idempotencyKey":"itest-key-1"}
                """;
        String first = mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String retry = mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(idOf(retry)).isEqualTo(idOf(first));
    }

    @Test
    void getCurrent_whenNoActiveSubscription_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/3/subscription"))
                .andExpect(status().isNotFound());
    }

    @Test
    void subscribeBeyondEligibility_isBlocked() throws Exception {
        // User 1 has no evaluation → eligible to base tier only; Gold is gated.
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(subscribe(1, 1, 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not unlocked")));
    }

    @Test
    void secondActiveSubscription_isRejected() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(subscribe(1, 1, 1)))
                .andExpect(status().isCreated());

        // One-active-per-user guard (friendly pre-check) — kept last (poisons the tx).
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content(subscribe(1, 2, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already has an active")));
    }

    private long lastSubscriptionId(long userId) throws Exception {
        String json = mockMvc.perform(get("/api/v1/users/{id}/subscription", userId))
                .andReturn().getResponse().getContentAsString();
        return idOf(json);
    }

    private long idOf(String json) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(json).get("id").asLong();
    }
}
