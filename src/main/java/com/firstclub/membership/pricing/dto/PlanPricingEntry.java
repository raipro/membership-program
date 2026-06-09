package com.firstclub.membership.pricing.dto;

import com.firstclub.membership.plan.BillingPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * One plan and the set of tiers purchasable under it, with their prices.
 */
@Getter
@AllArgsConstructor
public class PlanPricingEntry {

    private final Long planId;
    private final String code;
    private final String name;
    private final BillingPeriod billingPeriod;
    private final int durationDays;
    private final List<TierPriceEntry> tiers;
}
