package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.CriterionType;
import com.firstclub.membership.tierevaluation.TierCriterion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Satisfied when the user's total spend in the period meets the criterion's
 * inclusive minimum.
 */
@Component
public class MonthlySpendEvaluator implements CriterionEvaluator {

    @Override
    public CriterionType supportedType() {
        return CriterionType.MONTHLY_SPEND;
    }

    @Override
    public boolean isSatisfied(TierCriterion criterion, EvaluationContext context) {
        if (criterion.getThreshold() == null) {
            throw new IllegalStateException("MONTHLY_SPEND criterion " + criterion.getId() + " has no threshold");
        }
        return context.getTotalSpend().compareTo(criterion.getThreshold()) >= 0;
    }
}
