package com.firstclub.membership.subscription;

import com.firstclub.membership.common.exception.BusinessRuleException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.plan.PlanRepository;
import com.firstclub.membership.pricing.PlanTierPrice;
import com.firstclub.membership.pricing.PlanTierPriceRepository;
import com.firstclub.membership.subscription.dto.MembershipSubscriptionRequest;
import com.firstclub.membership.subscription.dto.MembershipSubscriptionResponse;
import com.firstclub.membership.subscription.eligibility.TierEligibilityService;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.TierRepository;
import com.firstclub.membership.user.UserAccount;
import com.firstclub.membership.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Subscribe and current-membership lookups.
 *
 * <p>Subscribe enforces the tier eligibility gate, snapshots the authoritative
 * plan×tier price, and is hardened against duplicate/concurrent requests:
 * <ul>
 *   <li>idempotency key — a retry with the same key returns the original subscription;</li>
 *   <li>a friendly pre-check for an existing active subscription;</li>
 *   <li>the {@code active_user_key} unique constraint as the race-proof backstop.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final TierRepository tierRepository;
    private final PlanTierPriceRepository priceRepository;
    private final TierEligibilityService eligibilityService;
    private final Clock clock;

    @Transactional
    public MembershipSubscriptionResponse subscribe(MembershipSubscriptionRequest request) {
        // 1. Idempotent retry: same key → return the original subscription, do not re-create.
        if (request.getIdempotencyKey() != null) {
            var existing = subscriptionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        UserAccount user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", request.getUserId()));
        MembershipPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> ResourceNotFoundException.of("Plan", request.getPlanId()));
        MembershipTier tier = tierRepository.findById(request.getTierId())
                .orElseThrow(() -> ResourceNotFoundException.of("Tier", request.getTierId()));

        if (!plan.isActive()) {
            throw new BusinessRuleException("Plan '%s' is not available".formatted(plan.getCode()));
        }
        if (!tier.isActive()) {
            throw new BusinessRuleException("Tier '%s' is not available".formatted(tier.getCode()));
        }

        // 2. Eligibility gate: requested tier must be within the user's earned ceiling.
        int ceiling = eligibilityService.eligibleRankCeiling(user);
        if (tier.getRank() > ceiling) {
            throw new BusinessRuleException(
                    "Tier '%s' is not unlocked yet; you are currently eligible up to tier rank %d"
                            .formatted(tier.getCode(), ceiling));
        }

        // 3. Authoritative price for this plan+tier; absence means the combo isn't purchasable.
        PlanTierPrice price = priceRepository
                .findByPlanIdAndTierIdAndActiveTrue(plan.getId(), tier.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Plan '%s' with tier '%s' is not purchasable".formatted(plan.getCode(), tier.getCode())));

        // 4. Friendly pre-check (the unique constraint below is the real guard under races).
        if (subscriptionRepository.findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE).isPresent()) {
            throw new BusinessRuleException("User already has an active subscription");
        }

        LocalDate startDate = LocalDate.now(clock);
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());
        boolean autoRenew = request.getAutoRenew() == null || request.getAutoRenew();

        Subscription subscription = Subscription.createActive(
                user, plan, tier, price.getPrice(), price.getCurrency(),
                startDate, endDate, autoRenew, request.getIdempotencyKey());

        try {
            subscriptionRepository.saveAndFlush(subscription);
        } catch (DataIntegrityViolationException e) {
            return handleConstraintRace(request, e);
        }
        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public MembershipSubscriptionResponse getCurrentSubscription(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException.of("User", userId);
        }
        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active subscription for user '%d'".formatted(userId)));
        return toResponse(subscription);
    }

    /**
     * A unique-constraint violation under concurrency means either a same-key retry won
     * the race (return that subscription) or a second active subscription was attempted.
     */
    private MembershipSubscriptionResponse handleConstraintRace(MembershipSubscriptionRequest request,
                                                               DataIntegrityViolationException e) {
        if (request.getIdempotencyKey() != null) {
            var existing = subscriptionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }
        throw new BusinessRuleException("User already has an active subscription");
    }

    private MembershipSubscriptionResponse toResponse(Subscription s) {
        long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(clock), s.getEndDate()));
        return new MembershipSubscriptionResponse(
                s.getId(),
                s.getUser().getId(),
                s.getPlan().getCode(),
                s.getPlan().getName(),
                s.getPlan().getBillingPeriod(),
                s.getTier().getCode(),
                s.getTier().getName(),
                s.getStatus(),
                s.getStartDate(),
                s.getEndDate(),
                daysRemaining,
                s.getPrice(),
                s.getCurrency(),
                s.isAutoRenew());
    }
}
