package com.firstclub.membership.plan;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.common.mapper.MembershipMapper;
import com.firstclub.membership.plan.dto.MembershipPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read access to the membership plan catalog.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;
    private final MembershipMapper mapper;

    /** All active plans, ordered shortest → longest duration. */
    public List<MembershipPlanResponse> getActivePlans() {
        return mapper.toPlanResponseList(planRepository.findByActiveTrueOrderByDurationDaysAsc());
    }

    public MembershipPlanResponse getPlan(Long id) {
        MembershipPlan plan = planRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Plan", id));
        return mapper.toPlanResponse(plan);
    }
}
