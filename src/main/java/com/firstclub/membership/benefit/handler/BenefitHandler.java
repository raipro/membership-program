package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;

/**
 * Strategy for interpreting a benefit's per-tier metadata into a normalized,
 * presentable form. One implementation per {@link BenefitType}.
 *
 * <p>This is the extensibility seam: a new perk is added by writing a new handler
 * (a Spring {@code @Component}) — the registry picks it up automatically and no
 * existing code changes. Future checkout-time computation (e.g. applying the
 * discount to a cart) also belongs behind this interface.
 */
public interface BenefitHandler {

    /** The benefit type this handler is responsible for. */
    BenefitType supportedType();

    /**
     * Resolve the benefit metadata into a summary + normalized attributes. Reads
     * required keys through the strict {@link MetadataAccessor}; any missing key,
     * wrong type, or invalid value raises
     * {@link com.firstclub.membership.benefit.BenefitMetadataException}.
     */
    ResolvedBenefit resolve(MetadataAccessor metadata);
}
