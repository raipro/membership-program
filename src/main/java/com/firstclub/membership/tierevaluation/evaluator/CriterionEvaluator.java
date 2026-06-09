package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.CriterionType;
import com.firstclub.membership.tierevaluation.TierCriterion;

/**
 * Strategy that decides whether a user satisfies one configured {@link TierCriterion}.
 * One implementation per {@link CriterionType}; the registry dispatches by type.
 *
 * <p>The extensibility seam for tier eligibility: a new way to earn a tier is added by
 * writing a new evaluator {@code @Component} — no existing code changes.
 */
public interface CriterionEvaluator {

    CriterionType supportedType();

    boolean isSatisfied(TierCriterion criterion, EvaluationContext context);
}
