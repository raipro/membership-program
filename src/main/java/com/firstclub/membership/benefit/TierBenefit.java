package com.firstclub.membership.benefit;

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

import static lombok.AccessLevel.PROTECTED;

/**
 * Links a {@link Benefit} to a {@link MembershipTier} with per-tier configuration.
 *
 * <p>{@code benefitMetadata} holds type-specific settings as JSON text (portable across
 * H2/Postgres) — e.g. {@code {"percentage": 10}} for a discount, or
 * {@code {"minOrderValue": 0}} for free delivery. This is what makes "each tier
 * unlocks additional perks — configurable" data-driven rather than code-driven.
 */
@Entity
@Table(
        name = "tier_benefit",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_tier_benefit",
                columnNames = {"tier_id", "benefit_id"}))
@Getter
@NoArgsConstructor(access = PROTECTED)
public class TierBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "benefit_id", nullable = false)
    private Benefit benefit;

    /** Type-specific configuration as JSON text; interpreted by the benefit's handler. */
    @Column(name = "benefit_metadata", columnDefinition = "text")
    private String benefitMetadata;
}
