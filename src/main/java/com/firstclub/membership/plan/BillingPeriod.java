package com.firstclub.membership.plan;

/**
 * Billing cadence of a membership plan. The canonical duration is carried on the
 * {@link MembershipPlan} itself ({@code durationDays}) so the enum stays free of
 * calendar math and new cadences can be added without code changes elsewhere.
 */
public enum BillingPeriod {
    MONTHLY,
    QUARTERLY,
    YEARLY
}
