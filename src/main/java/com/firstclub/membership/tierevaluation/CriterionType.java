package com.firstclub.membership.tierevaluation;

/**
 * Kinds of tier eligibility criteria. Each has a dedicated
 * {@link com.firstclub.membership.tierevaluation.evaluator.CriterionEvaluator}.
 * Adding a new criterion = new enum value + new evaluator, no changes elsewhere.
 */
public enum CriterionType {
    /** Minimum number of orders in the current period. */
    ORDER_COUNT,
    /** Minimum total order value in the current period. */
    MONTHLY_SPEND,
    /** Membership of an allowed cohort. */
    COHORT
}
