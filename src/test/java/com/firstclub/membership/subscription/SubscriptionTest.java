package com.firstclub.membership.subscription;

import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.user.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link Subscription} state machine: the guarded transitions and
 * the per-user active-slot invariant. Collaborators are mocked (their constructors are
 * package-protected) since only {@code user.getId()} matters here.
 */
class SubscriptionTest {

    private final UserAccount user = mock(UserAccount.class);
    private final MembershipPlan plan = mock(MembershipPlan.class);
    private final MembershipTier tier = mock(MembershipTier.class);

    private Subscription activeSubscription() {
        when(user.getId()).thenReturn(1L);
        return Subscription.createActive(user, plan, tier, new BigDecimal("99.00"), "INR",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), true, null);
    }

    @Test
    void createActive_isActive_andClaimsPerUserSlot() {
        Subscription s = activeSubscription();

        assertThat(s.isActive()).isTrue();
        assertThat(s.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(s.getActiveUserKey()).isEqualTo(1L);
    }

    @Test
    void cancel_movesToCancelled_andReleasesSlot() {
        Subscription s = activeSubscription();

        s.cancel();

        assertThat(s.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(s.getActiveUserKey()).isNull();
    }

    @Test
    void expire_movesToExpired_andReleasesSlot() {
        Subscription s = activeSubscription();

        s.expire();

        assertThat(s.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(s.getActiveUserKey()).isNull();
    }

    @Test
    void changeTier_updatesTierAndPrice_whileStayingActive() {
        Subscription s = activeSubscription();
        MembershipTier gold = mock(MembershipTier.class);

        s.changeTier(gold, new BigDecimal("199.00"), "INR");

        assertThat(s.getTier()).isSameAs(gold);
        assertThat(s.getPrice()).isEqualByComparingTo("199.00");
        assertThat(s.isActive()).isTrue();
    }

    @Test
    void changeTier_whenNotActive_throws() {
        Subscription s = activeSubscription();
        s.cancel();
        MembershipTier gold = mock(MembershipTier.class);

        assertThatThrownBy(() -> s.changeTier(gold, new BigDecimal("199.00"), "INR"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancel_whenAlreadyTerminal_throws() {
        Subscription s = activeSubscription();
        s.cancel();

        assertThatThrownBy(s::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expire_whenAlreadyTerminal_throws() {
        Subscription s = activeSubscription();
        s.cancel();

        assertThatThrownBy(s::expire).isInstanceOf(IllegalStateException.class);
    }
}
