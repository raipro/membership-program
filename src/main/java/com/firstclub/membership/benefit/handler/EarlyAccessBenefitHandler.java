package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Early access to sales plus access to exclusive deals.
 * Metadata: {@code {"earlyAccessHours": <number>}} (required; 0 = exclusive deals only).
 */
@Component
public class EarlyAccessBenefitHandler implements BenefitHandler {

    @Override
    public BenefitType supportedType() {
        return BenefitType.EARLY_ACCESS;
    }

    @Override
    public ResolvedBenefit resolve(MetadataAccessor metadata) {
        int earlyAccessHours = Math.max(0, metadata.requireInt("earlyAccessHours"));

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("earlyAccessHours", earlyAccessHours);
        attributes.put("exclusiveDeals", true);

        String summary = earlyAccessHours > 0
                ? "%dh early access to sales + exclusive deals".formatted(earlyAccessHours)
                : "Access to exclusive deals";
        return new ResolvedBenefit(summary, attributes);
    }
}
