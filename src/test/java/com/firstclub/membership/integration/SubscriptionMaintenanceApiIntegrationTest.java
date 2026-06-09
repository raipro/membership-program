package com.firstclub.membership.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test for the maintenance sweep over the seeded past-due subscriptions
 * (users 4, 5, 6). Verifies renew-keep, renew-with-downgrade, and expire.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubscriptionMaintenanceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sweepRenewsAndExpiresDueSubscriptions() throws Exception {
        mockMvc.perform(post("/api/v1/admin/subscriptions/sweep"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(3))
                .andExpect(jsonPath("$.renewed").value(2))
                .andExpect(jsonPath("$.expired").value(1));

        // User 4 (auto-renew, eligible Silver) → renewed, keeps Silver.
        mockMvc.perform(get("/api/v1/users/4/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tierCode").value("SILVER"));

        // User 6 (auto-renew Gold, no longer eligible) → renewed but downgraded to Silver.
        mockMvc.perform(get("/api/v1/users/6/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tierCode").value("SILVER"))
                .andExpect(jsonPath("$.price").value(99.00));

        // User 5 (no auto-renew) → expired → no active subscription. Last op (poisons tx).
        mockMvc.perform(get("/api/v1/users/5/subscription"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reevaluateAllUsers_processesEveryUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/tiers/reevaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usersEvaluated").value(6));
    }
}
