package com.firstclub.membership.subscription;

import com.firstclub.membership.subscription.dto.MembershipReevaluationResponse;
import com.firstclub.membership.subscription.dto.MembershipSweepResponse;
import com.firstclub.membership.tierevaluation.TierEvaluationService;
import com.firstclub.membership.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the maintenance jobs (driven by both the scheduler and admin endpoints).
 *
 * <p>Each due subscription is processed in its own transaction via
 * {@link SubscriptionService#processDueSubscription(Long)} (a separate bean, so the
 * transactional proxy applies), and one failure is logged without aborting the batch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionMaintenanceService {

    private final SubscriptionService subscriptionService;
    private final TierEvaluationService tierEvaluationService;
    private final UserRepository userRepository;

    public MembershipSweepResponse sweepDueSubscriptions() {
        List<Long> dueIds = subscriptionService.findDueSubscriptionIds();
        int renewed = 0;
        int expired = 0;
        for (Long id : dueIds) {
            try {
                DueOutcome outcome = subscriptionService.processDueSubscription(id);
                if (outcome == DueOutcome.RENEWED) {
                    renewed++;
                } else if (outcome == DueOutcome.EXPIRED) {
                    expired++;
                }
            } catch (Exception e) {
                log.error("Failed to process due subscription {}", id, e);
            }
        }
        log.info("Subscription sweep complete: processed={}, renewed={}, expired={}",
                dueIds.size(), renewed, expired);
        return new MembershipSweepResponse(dueIds.size(), renewed, expired);
    }

    public MembershipReevaluationResponse reevaluateAllUsers() {
        List<Long> userIds = userRepository.findAllIds();
        int evaluated = 0;
        for (Long userId : userIds) {
            try {
                tierEvaluationService.evaluate(userId);
                evaluated++;
            } catch (Exception e) {
                log.error("Failed to re-evaluate tier for user {}", userId, e);
            }
        }
        log.info("Tier re-evaluation complete: usersEvaluated={}", evaluated);
        return new MembershipReevaluationResponse(evaluated);
    }
}
