package com.firstclub.membership.pricing;

import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.tier.MembershipTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static lombok.AccessLevel.PROTECTED;

/**
 * Authoritative price for a specific (plan, tier) combination — the source of truth
 * for what a user pays when subscribing. One active row per plan+tier pair.
 *
 * <p>Not every (plan, tier) combination needs to exist; a missing/inactive row means
 * that combination is not purchasable (enforced at subscribe time in Task 4).
 */
@Entity
@Table(
        name = "plan_tier_price",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_plan_tier",
                columnNames = {"plan_id", "tier_id"}))
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PlanTierPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean active;
}
