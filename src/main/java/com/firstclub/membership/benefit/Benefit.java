package com.firstclub.membership.benefit;

import com.firstclub.membership.common.domain.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

/**
 * A catalog of available perks. A benefit is type + description only; the actual
 * per-tier behaviour (e.g. the discount percentage) is configured on the
 * {@link TierBenefit} link, so the same benefit can be tuned differently per tier.
 */
@Entity
@Table(name = "benefit")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Benefit extends AuditEntity {

    /** Stable business code, e.g. FREE_DELIVERY / EXTRA_DISCOUNT. */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BenefitType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active;
}
