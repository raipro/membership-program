package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.user.UserAccount;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * The snapshot of a user's profile + current-period order rollup that criteria are
 * evaluated against. Built once per evaluation and shared across all evaluators.
 */
@Getter
public class EvaluationContext {

    private final UserAccount user;
    private final int orderCount;
    private final BigDecimal totalSpend;

    public EvaluationContext(UserAccount user, int orderCount, BigDecimal totalSpend) {
        this.user = user;
        this.orderCount = orderCount;
        this.totalSpend = totalSpend;
    }
}
