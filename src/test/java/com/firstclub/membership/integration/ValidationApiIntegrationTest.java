package com.firstclub.membership.integration;

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
 * Bean-validation rejections → 400 with field-level details (validation fails before
 * the service, so the test transaction is never touched).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void subscribe_missingPlanId_returns400WithFieldDetail() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":1,"tierId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details[0]").value(containsString("planId")));
    }

    @Test
    void recordOrder_nonPositiveAmount_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/users/1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
