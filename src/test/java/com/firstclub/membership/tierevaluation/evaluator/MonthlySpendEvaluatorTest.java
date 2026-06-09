package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.TierCriterion;
import com.firstclub.membership.user.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthlySpendEvaluatorTest {

    private final MonthlySpendEvaluator evaluator = new MonthlySpendEvaluator();

    private EvaluationContext ctx(String spend) {
        return new EvaluationContext(mock(UserAccount.class), 0, new BigDecimal(spend));
    }

    private TierCriterion criterion(BigDecimal threshold) {
        TierCriterion c = mock(TierCriterion.class);
        when(c.getThreshold()).thenReturn(threshold);
        return c;
    }

    @Test
    void satisfied_whenSpendMeetsThreshold() {
        assertThat(evaluator.isSatisfied(criterion(new BigDecimal("3000")), ctx("3000"))).isTrue();
        assertThat(evaluator.isSatisfied(criterion(new BigDecimal("3000")), ctx("5000.50"))).isTrue();
    }

    @Test
    void notSatisfied_whenBelowThreshold() {
        assertThat(evaluator.isSatisfied(criterion(new BigDecimal("3000")), ctx("2999.99"))).isFalse();
    }

    @Test
    void throws_whenThresholdMissing() {
        assertThatThrownBy(() -> evaluator.isSatisfied(criterion(null), ctx("9999")))
                .isInstanceOf(IllegalStateException.class);
    }
}
