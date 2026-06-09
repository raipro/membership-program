package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Free delivery, free above a minimum order value (0 = all orders).
 * Metadata: {@code {"minOrderValue": <number>}} (required).
 */
@Component
public class FreeDeliveryBenefitHandler implements BenefitHandler {

    @Override
    public BenefitType supportedType() {
        return BenefitType.FREE_DELIVERY;
    }

    @Override
    public ResolvedBenefit resolve(MetadataAccessor metadata) {
        double minOrderValue = metadata.requireDouble("minOrderValue");

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("minOrderValue", minOrderValue);
        attributes.put("eligibleOrdersOnly", minOrderValue > 0);

        String summary = minOrderValue > 0
                ? "Free delivery on orders above %.0f".formatted(minOrderValue)
                : "Free delivery on all eligible orders";
        return new ResolvedBenefit(summary, attributes);
    }
}
