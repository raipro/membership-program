package com.firstclub.membership.subscription;

import com.firstclub.membership.subscription.dto.MembershipReevaluationResponse;
import com.firstclub.membership.subscription.dto.MembershipSweepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints to trigger the maintenance jobs on demand (the same logic the
 * scheduler runs). Useful for demos and operational reruns.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class SubscriptionMaintenanceController {

    private final SubscriptionMaintenanceService maintenanceService;

    @PostMapping("/subscriptions/sweep")
    public MembershipSweepResponse sweep() {
        return maintenanceService.sweepDueSubscriptions();
    }

    @PostMapping("/tiers/reevaluate")
    public MembershipReevaluationResponse reevaluate() {
        return maintenanceService.reevaluateAllUsers();
    }
}
