package com.firstclub.membership.tier;

import com.firstclub.membership.common.domain.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

/**
 * A membership <strong>tier</strong> — the benefit axis (Silver / Gold / Platinum).
 *
 * <p>Modeled as an entity (not an enum) because the spec requires tiers, their perks,
 * and their eligibility thresholds to be configurable, and because pricing, benefits,
 * and criteria all FK to a tier. Adding a new tier is a seed row, not a redeploy.
 *
 * <p>{@code rank} defines the upgrade/downgrade order and is used by tier evaluation
 * (Task 6) to compare eligibility (higher = more premium).
 */
@Entity
@Table(name = "membership_tier")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class MembershipTier extends AuditEntity {

    /** Stable business code, e.g. SILVER / GOLD / PLATINUM. */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false)
    private String name;

    /**
     * Ordinal position (higher = more premium). Mapped to {@code tier_rank}
     * because {@code rank} is a reserved word in PostgreSQL/H2.
     */
    @Column(name = "tier_rank", nullable = false)
    private int rank;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean active;
}
