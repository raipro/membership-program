package com.firstclub.membership.tierevaluation;

import com.firstclub.membership.tierevaluation.dto.MembershipEligibilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Triggers tier (re-)evaluation and exposes a user's current eligibility ceiling.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/tier")
@RequiredArgsConstructor
public class TierEvaluationController {

    private final TierEvaluationService tierEvaluationService;

    @PostMapping("/evaluate")
    public MembershipEligibilityResponse evaluate(@PathVariable Long userId) {
        return tierEvaluationService.evaluate(userId);
    }

    @GetMapping("/eligibility")
    public MembershipEligibilityResponse getEligibility(@PathVariable Long userId) {
        return tierEvaluationService.getEligibility(userId);
    }
}
