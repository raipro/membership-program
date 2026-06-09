package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.CriterionType;
import com.firstclub.membership.tierevaluation.TierCriterion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Satisfied when the user's order count meets the criterion's inclusive minimum.
 */
@Component
public class OrderCountEvaluator implements CriterionEvaluator {

    @Override
    public CriterionType supportedType() {
        return CriterionType.ORDER_COUNT;
    }

    @Override
    public boolean isSatisfied(TierCriterion criterion, EvaluationContext context) {
        BigDecimal threshold = requireThreshold(criterion);
        return BigDecimal.valueOf(context.getOrderCount()).compareTo(threshold) >= 0;
    }

    private BigDecimal requireThreshold(TierCriterion criterion) {
        if (criterion.getThreshold() == null) {
            throw new IllegalStateException("ORDER_COUNT criterion " + criterion.getId() + " has no threshold");
        }
        return criterion.getThreshold();
    }
}
