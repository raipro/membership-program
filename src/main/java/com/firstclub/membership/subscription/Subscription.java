package com.firstclub.membership.subscription;

import com.firstclub.membership.common.domain.AuditEntity;
import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static lombok.AccessLevel.PROTECTED;

/**
 * A user's membership subscription: a (plan, tier) pair active for a time window.
 *
 * <p>Rich domain model — state transitions go through the methods here (not setters),
 * so invariants (e.g. "only ACTIVE holds the per-user slot") are enforced in one place.
 * The surrounding workflow (audit, events, proration) is the service layer's job.
 *
 * <p><b>Price</b> is snapshotted at subscribe time, so later catalog price changes
 * don't retroactively alter an existing subscription.
 *
 * <p><b>Concurrency:</b> {@code version} gives optimistic locking; {@code activeUserKey}
 * (= userId while ACTIVE, null otherwise) carries a unique constraint, enforcing
 * one active subscription per user at the database level even under races.
 */
@Entity
@Table(name = "subscription")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Subscription extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Price snapshot taken at subscribe time. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    /** = userId while ACTIVE, null otherwise. Unique → one active subscription per user. */
    @Column(name = "active_user_key", unique = true)
    private Long activeUserKey;

    /** Optional client-supplied key for safe retries; unique when present. */
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Version
    @Column(nullable = false)
    private Long version;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Create a new ACTIVE subscription. The per-user slot ({@code activeUserKey}) is
     * claimed here; the unique constraint rejects a second concurrent active subscription.
     */
    public static Subscription createActive(UserAccount user, MembershipPlan plan, MembershipTier tier,
                                            BigDecimal price, String currency,
                                            LocalDate startDate, LocalDate endDate,
                                            boolean autoRenew, String idempotencyKey) {
        Subscription s = new Subscription();
        s.user = user;
        s.plan = plan;
        s.tier = tier;
        s.price = price;
        s.currency = currency;
        s.startDate = startDate;
        s.endDate = endDate;
        s.autoRenew = autoRenew;
        s.idempotencyKey = idempotencyKey;
        s.status = SubscriptionStatus.ACTIVE;
        s.activeUserKey = user.getId();
        return s;
    }

    /** ACTIVE → CANCELLED, releasing the per-user active slot. */
    public void cancel() {
        ensureActive("cancel");
        this.status = SubscriptionStatus.CANCELLED;
        this.activeUserKey = null;
    }

    /** ACTIVE → EXPIRED, releasing the per-user active slot. */
    public void expire() {
        ensureActive("expire");
        this.status = SubscriptionStatus.EXPIRED;
        this.activeUserKey = null;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    private void ensureActive(String action) {
        if (!isActive()) {
            throw new IllegalStateException(
                    "Cannot %s a subscription in status %s".formatted(action, status));
        }
    }
}
