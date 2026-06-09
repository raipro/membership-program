package com.firstclub.membership.pricing;

import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.pricing.dto.MembershipPricingResponse;
import com.firstclub.membership.pricing.dto.PlanPricingEntry;
import com.firstclub.membership.pricing.dto.TierPriceEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembles the purchasable plan × tier pricing matrix.
 *
 * <p>This is aggregation logic (grouping price rows by plan), so the nested view
 * DTOs are built here rather than via MapStruct, which is reserved for flat
 * entity → DTO field mapping.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingService {

    private static final String DEFAULT_CURRENCY = "INR";

    private final PlanTierPriceRepository priceRepository;

    public MembershipPricingResponse getPricingMatrix() {
        List<PlanTierPrice> prices = priceRepository.findPurchasableMatrix();

        // Preserve the repository ordering (plan duration asc, tier rank asc) while grouping.
        Map<Long, PlanGroup> byPlan = new LinkedHashMap<>();
        String currency = DEFAULT_CURRENCY;

        for (PlanTierPrice price : prices) {
            currency = price.getCurrency();
            MembershipPlan plan = price.getPlan();
            PlanGroup group = byPlan.computeIfAbsent(plan.getId(), id -> new PlanGroup(plan));
            group.tiers.add(new TierPriceEntry(
                    price.getTier().getId(),
                    price.getTier().getCode(),
                    price.getTier().getName(),
                    price.getTier().getRank(),
                    price.getPrice()));
        }

        List<PlanPricingEntry> planEntries = byPlan.values().stream()
                .map(PlanGroup::toEntry)
                .toList();

        return new MembershipPricingResponse(currency, planEntries);
    }

    /** Mutable accumulator used only while grouping price rows by plan. */
    private static final class PlanGroup {
        private final MembershipPlan plan;
        private final List<TierPriceEntry> tiers = new ArrayList<>();

        private PlanGroup(MembershipPlan plan) {
            this.plan = plan;
        }

        private PlanPricingEntry toEntry() {
            return new PlanPricingEntry(
                    plan.getId(),
                    plan.getCode(),
                    plan.getName(),
                    plan.getBillingPeriod(),
                    plan.getDurationDays(),
                    tiers);
        }
    }
}
