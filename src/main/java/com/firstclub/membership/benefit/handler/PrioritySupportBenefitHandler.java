package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Priority customer support for premium members.
 * Metadata: {@code {"channel": "EMAIL"|"CHAT"|"PHONE", "slaHours": <number>}} (both required).
 */
@Component
public class PrioritySupportBenefitHandler implements BenefitHandler {

    private static final Set<String> CHANNELS = Set.of("EMAIL", "CHAT", "PHONE");

    @Override
    public BenefitType supportedType() {
        return BenefitType.PRIORITY_SUPPORT;
    }

    @Override
    public ResolvedBenefit resolve(MetadataAccessor metadata) {
        String channel = metadata.requireOneOf("channel", CHANNELS);
        int slaHours = Math.max(1, metadata.requireInt("slaHours"));

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("channel", channel);
        attributes.put("slaHours", slaHours);

        String summary = "Priority support via %s (response SLA %dh)"
                .formatted(channel.toLowerCase(), slaHours);
        return new ResolvedBenefit(summary, attributes);
    }
}
