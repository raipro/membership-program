package com.firstclub.membership.subscription.eligibility;

import com.firstclub.membership.user.UserAccount;

/**
 * Authorization gate for tiers: the highest tier rank a user is currently eligible for.
 * A user may subscribe to / upgrade to / renew only a tier whose rank is at or below
 * this ceiling.
 *
 * <p>Consumer-owned interface (Dependency Inversion): the subscription module declares
 * the capability it needs. Task 4 ships a base-tier-only implementation; Task 6's tier
 * evaluation engine supplies the real criteria-driven ceiling behind this same seam.
 */
public interface TierEligibilityService {

    /**
     * @return the maximum tier rank {@code user} is eligible for (inclusive ceiling)
     */
    int eligibleRankCeiling(UserAccount user);
}
