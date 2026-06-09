package com.firstclub.membership.benefit.handler;

import lombok.Getter;

import java.util.Map;

/**
 * The output of a {@link BenefitHandler}: a human-readable summary plus the
 * normalized, type-specific attributes derived from a tier's benefit metadata.
 * Decoupled from both the JPA entity and the API DTO so handlers stay pure.
 */
@Getter
public class ResolvedBenefit {

    private final String summary;
    private final Map<String, Object> attributes;

    public ResolvedBenefit(String summary, Map<String, Object> attributes) {
        this.summary = summary;
        this.attributes = attributes;
    }
}
