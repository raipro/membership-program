package com.firstclub.membership.plan;

import com.firstclub.membership.common.domain.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static lombok.AccessLevel.PROTECTED;

/**
 * A membership <strong>plan</strong> — the billing axis (cadence + duration).
 *
 * <p>The {@code price} here is a list/reference "starting from" figure. The
 * authoritative price a user pays is resolved from the plan × tier matrix
 * ({@code plan_tier_price}); see the pricing package.
 */
@Entity
@Table(name = "membership_plan")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class MembershipPlan extends AuditEntity {

    /** Stable business code, e.g. MONTHLY / QUARTERLY / YEARLY. */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", nullable = false, length = 16)
    private BillingPeriod billingPeriod;

    /** Canonical plan length in days, used to compute subscription expiry. */
    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    /** Reference list price (starting-from); not the authoritative charge. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean active;
}
