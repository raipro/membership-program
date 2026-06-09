package com.firstclub.membership.tierevaluation;

import com.firstclub.membership.tier.MembershipTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static lombok.AccessLevel.PROTECTED;

/**
 * A configured eligibility rule attached to a tier. Data-driven so criteria can be
 * tuned (or new tiers gated) without code changes.
 *
 * <p>{@code threshold} is the inclusive minimum for numeric criteria (ORDER_COUNT /
 * MONTHLY_SPEND) and is null for COHORT, whose allowed cohorts live in
 * {@code criterionMetadata} as JSON, e.g. {@code {"cohorts": ["PREMIUM"]}}.
 */
@Entity
@Table(name = "tier_criterion")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class TierCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CriterionType type;

    /** Inclusive minimum for numeric criteria; null for COHORT. */
    @Column(precision = 14, scale = 2)
    private BigDecimal threshold;

    /** Type-specific JSON config (e.g. allowed cohorts); interpreted by the evaluator. */
    @Column(name = "criterion_metadata", columnDefinition = "text")
    private String criterionMetadata;
}
