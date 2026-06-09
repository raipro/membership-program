package com.firstclub.membership.tierevaluation.evaluator;

import com.firstclub.membership.tierevaluation.CriterionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link CriterionEvaluator} bean and indexes it by {@link CriterionType}.
 * New criterion types are wired automatically when their evaluator is added.
 */
@Component
public class CriterionEvaluatorRegistry {

    private final Map<CriterionType, CriterionEvaluator> evaluatorsByType;

    public CriterionEvaluatorRegistry(List<CriterionEvaluator> evaluators) {
        this.evaluatorsByType = evaluators.stream()
                .collect(Collectors.toMap(CriterionEvaluator::supportedType, Function.identity()));
    }

    public CriterionEvaluator get(CriterionType type) {
        CriterionEvaluator evaluator = evaluatorsByType.get(type);
        if (evaluator == null) {
            throw new IllegalStateException("No CriterionEvaluator registered for type " + type);
        }
        return evaluator;
    }
}
