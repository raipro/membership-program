package com.firstclub.membership.user;

import com.firstclub.membership.common.domain.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

/**
 * Minimal user stub. In a real platform this would live in an identity service; here
 * it carries just enough to anchor subscriptions and to drive tier evaluation later
 * ({@code cohort} feeds the cohort criterion in Task 6).
 */
@Entity
@Table(name = "user_account")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserAccount extends AuditEntity {

    /** Stable external identifier from the upstream identity system. */
    @Column(name = "external_id", nullable = false, unique = true, length = 64)
    private String externalId;

    @Column(nullable = false)
    private String email;

    /** Marketing/segmentation cohort; one input to tier eligibility. */
    @Column(length = 32)
    private String cohort;
}
