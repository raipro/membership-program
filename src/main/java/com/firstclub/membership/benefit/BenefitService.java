package com.firstclub.membership.benefit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.dto.MembershipBenefitResponse;
import com.firstclub.membership.benefit.handler.BenefitHandlerRegistry;
import com.firstclub.membership.benefit.handler.MetadataAccessor;
import com.firstclub.membership.benefit.handler.ResolvedBenefit;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.tier.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Resolves the effective, configured benefits for a tier.
 *
 * <p>Orchestration: load the tier's {@link TierBenefit} links → parse each
 * {@code benefitMetadata} string into a strict {@link MetadataAccessor} → dispatch to
 * the type-specific {@link com.firstclub.membership.benefit.handler.BenefitHandler}
 * → assemble the API DTO. The metadata parsing lives here; handlers receive the
 * accessor and stay free of persistence concerns.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BenefitService {

    private final TierBenefitRepository tierBenefitRepository;
    private final TierRepository tierRepository;
    private final BenefitHandlerRegistry handlerRegistry;
    private final ObjectMapper objectMapper;

    /**
     * Effective benefits for a tier, with each benefit's metadata interpreted by
     * its handler. A tier with no active benefits yields an empty list (not an error).
     *
     * @throws ResourceNotFoundException if the tier does not exist
     * @throws BenefitMetadataException  if any benefit's metadata is malformed
     */
    public List<MembershipBenefitResponse> getBenefitsForTier(Long tierId) {
        if (!tierRepository.existsById(tierId)) {
            throw ResourceNotFoundException.of("Tier", tierId);
        }
        return tierBenefitRepository.findActiveByTierId(tierId).stream()
                .map(this::resolve)
                .toList();
    }

    private MembershipBenefitResponse resolve(TierBenefit tierBenefit) {
        Benefit benefit = tierBenefit.getBenefit();
        MetadataAccessor metadata = parseMetadata(tierBenefit.getBenefitMetadata());
        ResolvedBenefit resolved = handlerRegistry.get(benefit.getType()).resolve(metadata);
        return new MembershipBenefitResponse(
                benefit.getCode(),
                benefit.getType(),
                benefit.getDescription(),
                resolved.getSummary(),
                resolved.getAttributes());
    }

    /**
     * Parse stored metadata into a strict {@link MetadataAccessor}. Null/blank yields
     * an empty accessor (so any required key the handler reads will fail as malformed).
     * Unparseable JSON is itself treated as malformed metadata.
     */
    private MetadataAccessor parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return MetadataAccessor.of(objectMapper.createObjectNode());
        }
        try {
            return MetadataAccessor.of(objectMapper.readTree(metadata));
        } catch (JsonProcessingException e) {
            throw new BenefitMetadataException("invalid benefit metadata JSON: " + metadata, e);
        }
    }
}
