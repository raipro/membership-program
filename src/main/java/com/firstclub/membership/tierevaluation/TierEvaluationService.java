package com.firstclub.membership.tierevaluation;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.orderstats.UserOrderStats;
import com.firstclub.membership.orderstats.UserOrderStatsRepository;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.TierRepository;
import com.firstclub.membership.tierevaluation.dto.MembershipEligibilityResponse;
import com.firstclub.membership.tierevaluation.evaluator.CriterionEvaluatorRegistry;
import com.firstclub.membership.tierevaluation.evaluator.EvaluationContext;
import com.firstclub.membership.user.UserAccount;
import com.firstclub.membership.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * Computes the highest tier a user is eligible for (the authorization ceiling) by
 * running each tier's configured criteria against the user's current-period order
 * rollup + profile, and persists the result to {@link UserTierStatus}.
 *
 * <p>A tier with no criteria is always satisfied (the base tier stays ungated). A tier
 * <em>with</em> criteria qualifies on <b>ANY</b> one of them (OR semantics) — e.g. cohort
 * membership alone can fast-track a tier. Flip {@code anyMatch}→{@code allMatch} below
 * to require all criteria.
 */
@Service
@RequiredArgsConstructor
public class TierEvaluationService {

    private final UserRepository userRepository;
    private final TierRepository tierRepository;
    private final TierCriterionRepository criterionRepository;
    private final UserOrderStatsRepository statsRepository;
    private final UserTierStatusRepository statusRepository;
    private final CriterionEvaluatorRegistry evaluatorRegistry;
    private final Clock clock;

    /** Re-evaluate the user's eligible tier and persist it. */
    @Transactional
    public MembershipEligibilityResponse evaluate(Long userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        EvaluationContext context = buildContext(user);
        MembershipTier eligibleTier = highestEligibleTier(context);

        Instant now = clock.instant();
        UserTierStatus status = statusRepository.findByUserId(userId)
                .map(existing -> {
                    existing.updateEligibility(eligibleTier, now);
                    return existing;
                })
                .orElseGet(() -> UserTierStatus.initial(user, eligibleTier, now));
        statusRepository.save(status);

        return toResponse(user, eligibleTier, now, context);
    }

    /** Read the persisted eligibility; defaults to the base tier if never evaluated. */
    @Transactional(readOnly = true)
    public MembershipEligibilityResponse getEligibility(Long userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        EvaluationContext context = buildContext(user);
        return statusRepository.findByUserId(userId)
                .map(status -> toResponse(user, status.getEligibleTier(), status.getEvaluatedAt(), context))
                .orElseGet(() -> toResponse(user, baseTier(), null, context));
    }

    private EvaluationContext buildContext(UserAccount user) {
        String period = YearMonth.now(clock).toString();
        int orderCount = statsRepository.findByUserIdAndPeriod(user.getId(), period)
                .map(UserOrderStats::getOrderCount).orElse(0);
        BigDecimal totalSpend = statsRepository.findByUserIdAndPeriod(user.getId(), period)
                .map(UserOrderStats::getTotalValue).orElse(BigDecimal.ZERO);
        return new EvaluationContext(user, orderCount, totalSpend);
    }

    private MembershipTier highestEligibleTier(EvaluationContext context) {
        return tierRepository.findByActiveTrueOrderByRankAsc().stream()
                .filter(tier -> isTierSatisfied(tier, context))
                .max(Comparator.comparingInt(MembershipTier::getRank))
                .orElseThrow(() -> new IllegalStateException("No active membership tiers configured"));
    }

    private boolean isTierSatisfied(MembershipTier tier, EvaluationContext context) {
        List<TierCriterion> criteria = criterionRepository.findByTierId(tier.getId());
        if (criteria.isEmpty()) {
            return true; // ungated base tier
        }
        // ANY criterion qualifies the user for this tier (OR). Flip to allMatch for ALL.
        return criteria.stream()
                .anyMatch(c -> evaluatorRegistry.get(c.getType()).isSatisfied(c, context));
    }

    private MembershipTier baseTier() {
        return tierRepository.findByActiveTrueOrderByRankAsc().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active membership tiers configured"));
    }

    private MembershipEligibilityResponse toResponse(UserAccount user, MembershipTier tier,
                                                    Instant evaluatedAt, EvaluationContext context) {
        return new MembershipEligibilityResponse(
                user.getId(),
                tier.getId(),
                tier.getCode(),
                tier.getName(),
                tier.getRank(),
                evaluatedAt,
                context.getOrderCount(),
                context.getTotalSpend(),
                user.getCohort());
    }
}
