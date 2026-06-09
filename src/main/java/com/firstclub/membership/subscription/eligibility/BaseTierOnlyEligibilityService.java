package com.firstclub.membership.subscription.eligibility;

import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.TierRepository;
import com.firstclub.membership.user.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Interim eligibility gate: every user is eligible for the base (lowest-rank) tier only.
 * This is correct for brand-new users with no order history and keeps subscribe gated
 * from day one.
 *
 * <p>Task 6 introduces a criteria-driven implementation; this bean will then be
 * superseded (the new one marked {@code @Primary} or this one removed).
 */
@Service
@RequiredArgsConstructor
public class BaseTierOnlyEligibilityService implements TierEligibilityService {

    private final TierRepository tierRepository;

    @Override
    public int eligibleRankCeiling(UserAccount user) {
        return tierRepository.findByActiveTrueOrderByRankAsc().stream()
                .findFirst()
                .map(MembershipTier::getRank)
                .orElseThrow(() -> new IllegalStateException("No active membership tiers configured"));
    }
}
