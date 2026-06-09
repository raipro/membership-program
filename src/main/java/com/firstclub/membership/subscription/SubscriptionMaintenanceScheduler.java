package com.firstclub.membership.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the maintenance jobs on a schedule. Cadences are configurable (defaults: daily).
 * The same jobs are exposed via admin endpoints so they can be triggered on demand.
 */
@Component
@RequiredArgsConstructor
public class SubscriptionMaintenanceScheduler {

    private final SubscriptionMaintenanceService maintenanceService;

    /** Expire/renew subscriptions whose term has ended. Default: daily at 01:00. */
    @Scheduled(cron = "${membership.scheduling.expiry-sweep-cron:0 0 1 * * *}")
    public void runExpirySweep() {
        maintenanceService.sweepDueSubscriptions();
    }

    /** Refresh every user's tier eligibility. Default: daily at 02:00. */
    @Scheduled(cron = "${membership.scheduling.reevaluation-cron:0 0 2 * * *}")
    public void runTierReevaluation() {
        maintenanceService.reevaluateAllUsers();
    }
}
