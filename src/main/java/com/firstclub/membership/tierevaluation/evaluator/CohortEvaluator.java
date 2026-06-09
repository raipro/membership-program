package com.firstclub.membership.tierevaluation.evaluator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.tierevaluation.CriterionType;
import com.firstclub.membership.tierevaluation.TierCriterion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Satisfied when the user's cohort is one of the criterion's allowed cohorts.
 * Metadata: {@code {"cohorts": ["PREMIUM", "VIP"]}}.
 */
@Component
@RequiredArgsConstructor
public class CohortEvaluator implements CriterionEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public CriterionType supportedType() {
        return CriterionType.COHORT;
    }

    @Override
    public boolean isSatisfied(TierCriterion criterion, EvaluationContext context) {
        String cohort = context.getUser().getCohort();
        if (cohort == null) {
            return false;
        }
        return allowedCohorts(criterion).contains(cohort);
    }

    private Set<String> allowedCohorts(TierCriterion criterion) {
        String metadata = criterion.getCriterionMetadata();
        if (metadata == null || metadata.isBlank()) {
            throw new IllegalStateException("COHORT criterion " + criterion.getId() + " has no cohorts configured");
        }
        try {
            JsonNode cohorts = objectMapper.readTree(metadata).path("cohorts");
            Set<String> allowed = new HashSet<>();
            cohorts.forEach(node -> allowed.add(node.asText()));
            if (allowed.isEmpty()) {
                throw new IllegalStateException("COHORT criterion " + criterion.getId() + " lists no cohorts");
            }
            return allowed;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid COHORT criterion metadata: " + metadata, e);
        }
    }
}
