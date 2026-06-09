package com.firstclub.membership.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * The full plan × tier pricing matrix the user chooses from when subscribing.
 * Assembled by {@link com.firstclub.membership.pricing.PricingService}.
 */
@Getter
@AllArgsConstructor
public class MembershipPricingResponse {

    private final String currency;
    private final List<PlanPricingEntry> plans;
}
