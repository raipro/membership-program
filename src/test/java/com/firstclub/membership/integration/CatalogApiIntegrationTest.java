package com.firstclub.membership.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for the read-only catalog APIs against the seeded H2 data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsActivePlansOrderedByDuration() throws Exception {
        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("MONTHLY"))
                .andExpect(jsonPath("$[2].code").value("YEARLY"));
    }

    @Test
    void listsActiveTiersOrderedByRank() throws Exception {
        mockMvc.perform(get("/api/v1/tiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("SILVER"))
                .andExpect(jsonPath("$[2].code").value("PLATINUM"));
    }

    @Test
    void tierDetailEmbedsResolvedBenefits() throws Exception {
        mockMvc.perform(get("/api/v1/tiers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GOLD"))
                .andExpect(jsonPath("$.benefits.length()").value(3));
    }

    @Test
    void silverBenefitResolvesThresholdDelivery() throws Exception {
        mockMvc.perform(get("/api/v1/tiers/1/benefits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("FREE_DELIVERY"))
                .andExpect(jsonPath("$[0].summary").value("Free delivery on orders above 500"));
    }

    @Test
    void pricingMatrixIsGroupedByPlan() throws Exception {
        mockMvc.perform(get("/api/v1/plans/pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.plans.length()").value(3))
                .andExpect(jsonPath("$.plans[0].code").value("MONTHLY"))
                .andExpect(jsonPath("$.plans[0].tiers[2].code").value("PLATINUM"))
                .andExpect(jsonPath("$.plans[0].tiers[2].price").value(399.00));
    }

    @Test
    void unknownTierReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/tiers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
