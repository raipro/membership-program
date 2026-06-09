package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.TierCriterion;
import com.firstclub.membership.user.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderCountEvaluatorTest {

    private final OrderCountEvaluator evaluator = new OrderCountEvaluator();

    private EvaluationContext ctx(int orderCount) {
        return new EvaluationContext(mock(UserAccount.class), orderCount, BigDecimal.ZERO);
    }

    private TierCriterion criterion(BigDecimal threshold) {
        TierCriterion c = mock(TierCriterion.class);
        when(c.getThreshold()).thenReturn(threshold);
        return c;
    }

    @Test
    void satisfied_whenCountMeetsThreshold() {
        assertThat(evaluator.isSatisfied(criterion(new BigDecimal("5")), ctx(5))).isTrue();
        assertThat(evaluator.isSatisfied(criterion(new BigDecimal("5")), ctx(6))).isTrue();
    }

    @Test
    void notSatisfied_whenBelowThreshold() {
        assertThat(evaluator.isSatisfied(criterion(new BigDecimal("5")), ctx(4))).isFalse();
    }

    @Test
    void throws_whenThresholdMissing() {
        assertThatThrownBy(() -> evaluator.isSatisfied(criterion(null), ctx(10)))
                .isInstanceOf(IllegalStateException.class);
    }
}
