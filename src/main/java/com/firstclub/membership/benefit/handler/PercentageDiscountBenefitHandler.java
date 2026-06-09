package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extra percentage discount on selected items/categories.
 * Metadata: {@code {"percentage": <0-100>, "appliesTo": "SELECTED_ITEMS"|"ALL_ITEMS"}} (both required).
 */
@Component
public class PercentageDiscountBenefitHandler implements BenefitHandler {

    private static final Set<String> SCOPES = Set.of("SELECTED_ITEMS", "ALL_ITEMS");

    @Override
    public BenefitType supportedType() {
        return BenefitType.PERCENTAGE_DISCOUNT;
    }

    @Override
    public ResolvedBenefit resolve(MetadataAccessor metadata) {
        // Clamp to a sane 0-100 range so a bad config can't produce nonsense discounts.
        int percentage = Math.max(0, Math.min(100, metadata.requireInt("percentage")));
        String appliesTo = metadata.requireOneOf("appliesTo", SCOPES);

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("percentage", percentage);
        attributes.put("appliesTo", appliesTo);

        String scopeLabel = "ALL_ITEMS".equals(appliesTo) ? "all items" : "selected items";
        String summary = "%d%% extra discount on %s".formatted(percentage, scopeLabel);
        return new ResolvedBenefit(summary, attributes);
    }
}
