package com.firstclub.membership.tierevaluation;

import com.firstclub.membership.subscription.eligibility.TierEligibilityService;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.TierRepository;
import com.firstclub.membership.user.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * The real eligibility gate, backed by tier evaluation: the ceiling is the rank of the
 * user's persisted {@link UserTierStatus}. A user who has never been evaluated defaults
 * to the base tier only.
 *
 * <p>{@code @Primary} so it supersedes the interim {@code BaseTierOnlyEligibilityService}
 * from Task 4 wherever {@link TierEligibilityService} is injected.
 */
@Service
@Primary
@RequiredArgsConstructor
public class EvaluatedTierEligibilityService implements TierEligibilityService {

    private final UserTierStatusRepository statusRepository;
    private final TierRepository tierRepository;

    @Override
    public int eligibleRankCeiling(UserAccount user) {
        return statusRepository.findByUserId(user.getId())
                .map(status -> status.getEligibleTier().getRank())
                .orElseGet(this::baseRank);
    }

    private int baseRank() {
        return tierRepository.findByActiveTrueOrderByRankAsc().stream()
                .findFirst()
                .map(MembershipTier::getRank)
                .orElseThrow(() -> new IllegalStateException("No active membership tiers configured"));
    }
}
