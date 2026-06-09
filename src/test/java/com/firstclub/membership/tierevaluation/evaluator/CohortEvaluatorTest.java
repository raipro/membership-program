package com.firstclub.membership.tierevaluation.evaluator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.tierevaluation.TierCriterion;
import com.firstclub.membership.user.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CohortEvaluatorTest {

    private final CohortEvaluator evaluator = new CohortEvaluator(new ObjectMapper());

    private EvaluationContext ctxForCohort(String cohort) {
        UserAccount user = mock(UserAccount.class);
        when(user.getCohort()).thenReturn(cohort);
        return new EvaluationContext(user, 0, BigDecimal.ZERO);
    }

    private TierCriterion criterion(String metadata) {
        TierCriterion c = mock(TierCriterion.class);
        when(c.getCriterionMetadata()).thenReturn(metadata);
        return c;
    }

    @Test
    void satisfied_whenUserCohortIsAllowed() {
        assertThat(evaluator.isSatisfied(criterion("{\"cohorts\": [\"PREMIUM\"]}"), ctxForCohort("PREMIUM")))
                .isTrue();
    }

    @Test
    void notSatisfied_whenUserCohortNotAllowed() {
        assertThat(evaluator.isSatisfied(criterion("{\"cohorts\": [\"PREMIUM\"]}"), ctxForCohort("REGULAR")))
                .isFalse();
    }

    @Test
    void notSatisfied_whenUserHasNoCohort() {
        assertThat(evaluator.isSatisfied(criterion("{\"cohorts\": [\"PREMIUM\"]}"), ctxForCohort(null)))
                .isFalse();
    }

    @Test
    void throws_whenCohortsMetadataMissing() {
        assertThatThrownBy(() -> evaluator.isSatisfied(criterion(null), ctxForCohort("PREMIUM")))
                .isInstanceOf(IllegalStateException.class);
    }
}
