package com.firstclub.membership.plan;

import com.firstclub.membership.plan.dto.MembershipPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read APIs for the membership plan catalog.
 */
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public List<MembershipPlanResponse> getPlans() {
        return planService.getActivePlans();
    }

    @GetMapping("/{id}")
    public MembershipPlanResponse getPlan(@PathVariable Long id) {
        return planService.getPlan(id);
    }
}
