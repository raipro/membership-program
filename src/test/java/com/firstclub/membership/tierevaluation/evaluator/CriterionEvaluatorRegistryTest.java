package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.CriterionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriterionEvaluatorRegistryTest {

    private final CriterionEvaluatorRegistry registry =
            new CriterionEvaluatorRegistry(List.of(new OrderCountEvaluator()));

    @Test
    void resolvesRegisteredEvaluatorByType() {
        assertThat(registry.get(CriterionType.ORDER_COUNT))
                .isInstanceOf(OrderCountEvaluator.class);
    }

    @Test
    void throws_forUnregisteredType() {
        assertThatThrownBy(() -> registry.get(CriterionType.COHORT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No CriterionEvaluator");
    }
}
