package com.firstclub.membership.tierevaluation;

import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

/**
 * The persisted result of tier evaluation: the highest tier a user is currently
 * eligible for (the authorization ceiling read by the eligibility gate). One row per user.
 */
@Entity
@Table(name = "user_tier_status")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserTierStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eligible_tier_id", nullable = false)
    private MembershipTier eligibleTier;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public static UserTierStatus initial(UserAccount user, MembershipTier eligibleTier, Instant evaluatedAt) {
        UserTierStatus status = new UserTierStatus();
        status.user = user;
        status.eligibleTier = eligibleTier;
        status.evaluatedAt = evaluatedAt;
        return status;
    }

    public void updateEligibility(MembershipTier eligibleTier, Instant evaluatedAt) {
        this.eligibleTier = eligibleTier;
        this.evaluatedAt = evaluatedAt;
    }
}
