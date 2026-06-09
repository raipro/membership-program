package com.firstclub.membership.subscription;

import com.firstclub.membership.common.exception.BusinessRuleException;
import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.plan.PlanRepository;
import com.firstclub.membership.pricing.PlanTierPrice;
import com.firstclub.membership.pricing.PlanTierPriceRepository;
import com.firstclub.membership.subscription.dto.MembershipSubscriptionRequest;
import com.firstclub.membership.subscription.eligibility.TierEligibilityService;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.TierRepository;
import com.firstclub.membership.user.UserAccount;
import com.firstclub.membership.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the upgrade/downgrade/cancel orchestration: direction validation, the
 * eligibility gate, and the cancel guard. Eligibility is mocked so the happy paths are
 * deterministic regardless of Task 6.
 */
class SubscriptionServiceTest {

    private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final TierRepository tierRepository = mock(TierRepository.class);
    private final PlanTierPriceRepository priceRepository = mock(PlanTierPriceRepository.class);
    private final TierEligibilityService eligibilityService = mock(TierEligibilityService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private final SubscriptionService service = new SubscriptionService(
            subscriptionRepository, userRepository, planRepository, tierRepository,
            priceRepository, eligibilityService, eventPublisher, clock);

    private final UserAccount user = mock(UserAccount.class);
    private final MembershipPlan plan = mock(MembershipPlan.class);

    private MembershipTier tier(long id, int rank) {
        MembershipTier t = mock(MembershipTier.class);
        when(t.getId()).thenReturn(id);
        when(t.getRank()).thenReturn(rank);
        return t;
    }

    private Subscription activeOn(MembershipTier currentTier) {
        when(user.getId()).thenReturn(1L);
        return Subscription.createActive(user, plan, currentTier, new BigDecimal("99.00"), "INR",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), true, null);
    }

    @Test
    void upgrade_changesTierAndSnapshotsNewPrice() {
        MembershipTier silver = tier(1L, 1);
        MembershipTier gold = tier(2L, 2);
        when(gold.isActive()).thenReturn(true);
        Subscription sub = activeOn(silver);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(2L)).thenReturn(Optional.of(gold));
        when(plan.getId()).thenReturn(1L);
        when(eligibilityService.eligibleRankCeiling(user)).thenReturn(3);
        PlanTierPrice price = mock(PlanTierPrice.class);
        when(price.getPrice()).thenReturn(new BigDecimal("199.00"));
        when(price.getCurrency()).thenReturn("INR");
        when(priceRepository.findByPlanIdAndTierIdAndActiveTrue(1L, 2L)).thenReturn(Optional.of(price));

        service.upgrade(10L, 2L);

        assertThat(sub.getTier()).isSameAs(gold);
        assertThat(sub.getPrice()).isEqualByComparingTo("199.00");
        assertThat(sub.isActive()).isTrue();
    }

    @Test
    void upgrade_whenTargetNotHigherRank_isRejected() {
        MembershipTier gold = tier(2L, 2);
        MembershipTier silver = tier(1L, 1);
        when(silver.isActive()).thenReturn(true);
        Subscription sub = activeOn(gold);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silver));

        assertThatThrownBy(() -> service.upgrade(10L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not an upgrade");
    }

    @Test
    void upgrade_whenBeyondEligibilityCeiling_isRejected() {
        MembershipTier silver = tier(1L, 1);
        MembershipTier gold = tier(2L, 2);
        when(gold.isActive()).thenReturn(true);
        when(gold.getCode()).thenReturn("GOLD");
        Subscription sub = activeOn(silver);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(2L)).thenReturn(Optional.of(gold));
        when(eligibilityService.eligibleRankCeiling(user)).thenReturn(1); // base only

        assertThatThrownBy(() -> service.upgrade(10L, 2L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not unlocked yet");
    }

    @Test
    void downgrade_changesToLowerTier() {
        MembershipTier gold = tier(2L, 2);
        MembershipTier silver = tier(1L, 1);
        when(silver.isActive()).thenReturn(true);
        Subscription sub = activeOn(gold);

        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silver));
        when(plan.getId()).thenReturn(1L);
        when(eligibilityService.eligibleRankCeiling(user)).thenReturn(3);
        PlanTierPrice price = mock(PlanTierPrice.class);
        when(price.getPrice()).thenReturn(new BigDecimal("99.00"));
        when(price.getCurrency()).thenReturn("INR");
        when(priceRepository.findByPlanIdAndTierIdAndActiveTrue(1L, 1L)).thenReturn(Optional.of(price));

        service.downgrade(10L, 1L);

        assertThat(sub.getTier()).isSameAs(silver);
        assertThat(sub.getPrice()).isEqualByComparingTo("99.00");
    }

    @Test
    void cancel_setsStatusCancelled() {
        MembershipTier silver = tier(1L, 1);
        Subscription sub = activeOn(silver);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        service.cancel(10L);

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(sub.getActiveUserKey()).isNull();
    }

    @Test
    void cancel_whenNotActive_isRejected() {
        MembershipTier silver = tier(1L, 1);
        Subscription sub = activeOn(silver);
        sub.cancel();
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> service.cancel(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("active subscription can be cancelled");
    }

    // --- subscribe edge branches (Task 4) ---

    @Test
    void subscribe_whenPlanInactive_isRejected() {
        MembershipTier silver = tier(1L, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silver));
        when(plan.isActive()).thenReturn(false);
        when(plan.getCode()).thenReturn("MONTHLY");

        assertThatThrownBy(() -> service.subscribe(request(1L, 1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void subscribe_whenTierInactive_isRejected() {
        MembershipTier silver = tier(1L, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silver));
        when(plan.isActive()).thenReturn(true);
        when(silver.isActive()).thenReturn(false);
        when(silver.getCode()).thenReturn("SILVER");

        assertThatThrownBy(() -> service.subscribe(request(1L, 1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void subscribe_whenPlanTierNotPurchasable_isRejected() {
        MembershipTier silver = tier(1L, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silver));
        when(plan.isActive()).thenReturn(true);
        when(plan.getId()).thenReturn(1L);
        when(plan.getCode()).thenReturn("MONTHLY");
        when(silver.isActive()).thenReturn(true);
        when(silver.getCode()).thenReturn("SILVER");
        when(eligibilityService.eligibleRankCeiling(user)).thenReturn(3);
        when(priceRepository.findByPlanIdAndTierIdAndActiveTrue(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.subscribe(request(1L, 1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not purchasable");
    }

    // --- changeTier edge branches (Task 5) ---

    @Test
    void upgrade_toSameTier_isRejected() {
        MembershipTier silver = tier(1L, 1);
        when(silver.isActive()).thenReturn(true);
        when(silver.getCode()).thenReturn("SILVER");
        Subscription sub = activeOn(silver);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silver));

        assertThatThrownBy(() -> service.upgrade(10L, 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already on tier");
    }

    @Test
    void downgrade_toHigherTier_isRejected() {
        MembershipTier silver = tier(1L, 1);
        MembershipTier gold = tier(2L, 2);
        when(gold.isActive()).thenReturn(true);
        when(gold.getCode()).thenReturn("GOLD");
        Subscription sub = activeOn(silver);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(2L)).thenReturn(Optional.of(gold));

        assertThatThrownBy(() -> service.downgrade(10L, 2L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not a downgrade");
    }

    @Test
    void changeToInactiveTier_isRejected() {
        MembershipTier silver = tier(1L, 1);
        MembershipTier gold = tier(2L, 2);
        when(gold.isActive()).thenReturn(false);
        when(gold.getCode()).thenReturn("GOLD");
        Subscription sub = activeOn(silver);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(tierRepository.findById(2L)).thenReturn(Optional.of(gold));

        assertThatThrownBy(() -> service.upgrade(10L, 2L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }

    // --- maintenance sweep branches (Task 7) ---

    @Test
    void processDue_autoRenew_keepsTierWhenStillEligible() {
        MembershipTier silver = tier(1L, 1);
        when(silver.getCode()).thenReturn("SILVER");
        Subscription sub = dueSub(silver, true);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(eligibilityService.eligibleRankCeiling(user)).thenReturn(1);
        when(plan.getId()).thenReturn(1L);
        when(plan.getDurationDays()).thenReturn(30);
        PlanTierPrice price = mock(PlanTierPrice.class);
        when(price.getPrice()).thenReturn(new BigDecimal("99.00"));
        when(price.getCurrency()).thenReturn("INR");
        when(priceRepository.findByPlanIdAndTierIdAndActiveTrue(1L, 1L)).thenReturn(Optional.of(price));

        DueOutcome outcome = service.processDueSubscription(10L);

        assertThat(outcome).isEqualTo(DueOutcome.RENEWED);
        assertThat(sub.getTier()).isSameAs(silver);
        assertThat(sub.getEndDate()).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void processDue_autoRenew_downgradesWhenNoLongerEligible() {
        MembershipTier silver = tier(1L, 1);
        MembershipTier gold = tier(2L, 2);
        Subscription sub = dueSub(gold, true);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));
        when(eligibilityService.eligibleRankCeiling(user)).thenReturn(1);
        when(tierRepository.findByActiveTrueOrderByRankAsc()).thenReturn(List.of(silver, gold));
        when(plan.getId()).thenReturn(1L);
        when(plan.getDurationDays()).thenReturn(30);
        PlanTierPrice price = mock(PlanTierPrice.class);
        when(price.getPrice()).thenReturn(new BigDecimal("99.00"));
        when(price.getCurrency()).thenReturn("INR");
        when(priceRepository.findByPlanIdAndTierIdAndActiveTrue(1L, 1L)).thenReturn(Optional.of(price));

        DueOutcome outcome = service.processDueSubscription(10L);

        assertThat(outcome).isEqualTo(DueOutcome.RENEWED);
        assertThat(sub.getTier()).isSameAs(silver);
    }

    @Test
    void processDue_noAutoRenew_expires() {
        MembershipTier silver = tier(1L, 1);
        Subscription sub = dueSub(silver, false);
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        DueOutcome outcome = service.processDueSubscription(10L);

        assertThat(outcome).isEqualTo(DueOutcome.EXPIRED);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    void processDue_notYetDue_isSkipped() {
        MembershipTier silver = tier(1L, 1);
        Subscription sub = activeOn(silver); // ends 2026-01-31; clock is 2026-01-01 → not due
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        assertThat(service.processDueSubscription(10L)).isEqualTo(DueOutcome.SKIPPED);
    }

    @Test
    void processDue_notActive_isSkipped() {
        MembershipTier silver = tier(1L, 1);
        Subscription sub = dueSub(silver, true);
        sub.cancel();
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(sub));

        assertThat(service.processDueSubscription(10L)).isEqualTo(DueOutcome.SKIPPED);
    }

    private MembershipSubscriptionRequest request(long userId, long planId, long tierId) {
        MembershipSubscriptionRequest r = new MembershipSubscriptionRequest();
        r.setUserId(userId);
        r.setPlanId(planId);
        r.setTierId(tierId);
        return r;
    }

    private Subscription dueSub(MembershipTier currentTier, boolean autoRenew) {
        when(user.getId()).thenReturn(1L);
        return Subscription.createActive(user, plan, currentTier, new BigDecimal("99.00"), "INR",
                LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31), autoRenew, null);
    }
}
