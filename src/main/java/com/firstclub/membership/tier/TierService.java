package com.firstclub.membership.tier;

import com.firstclub.membership.benefit.BenefitService;
import com.firstclub.membership.benefit.dto.MembershipBenefitResponse;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.common.mapper.MembershipMapper;
import com.firstclub.membership.tier.dto.MembershipTierDetailResponse;
import com.firstclub.membership.tier.dto.MembershipTierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read access to the membership tier catalog.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TierService {

    private final TierRepository tierRepository;
    private final BenefitService benefitService;
    private final MembershipMapper mapper;

    /** All active tiers, ordered by rank (Silver → Gold → Platinum). */
    public List<MembershipTierResponse> getActiveTiers() {
        return mapper.toTierResponseList(tierRepository.findByActiveTrueOrderByRankAsc());
    }

    /** A single tier with its resolved benefits embedded. */
    public MembershipTierDetailResponse getTier(Long id) {
        MembershipTier tier = tierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tier", id));
        List<MembershipBenefitResponse> benefits = benefitService.getBenefitsForTier(id);
        return new MembershipTierDetailResponse(
                tier.getId(),
                tier.getCode(),
                tier.getName(),
                tier.getRank(),
                tier.getDescription(),
                tier.isActive(),
                benefits);
    }
}
