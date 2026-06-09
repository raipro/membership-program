package com.firstclub.membership.subscription.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Demonstrative listener for subscription domain events. In a real system this seam
 * would fan out to notifications, analytics, or downstream services; here it logs,
 * showing that lifecycle changes are decoupled from their side effects.
 */
@Component
@Slf4j
public class SubscriptionEventListener {

    @EventListener
    public void onRenewed(SubscriptionRenewedEvent event) {
        log.info("Subscription {} (user {}) renewed at tier {} until {}",
                event.getSubscriptionId(), event.getUserId(), event.getTierCode(), event.getNewEndDate());
    }

    @EventListener
    public void onExpired(SubscriptionExpiredEvent event) {
        log.info("Subscription {} (user {}) expired", event.getSubscriptionId(), event.getUserId());
    }

    @EventListener
    public void onTierChanged(SubscriptionTierChangedEvent event) {
        log.info("Subscription {} (user {}) tier changed {} -> {}",
                event.getSubscriptionId(), event.getUserId(), event.getFromTierCode(), event.getToTierCode());
    }
}
