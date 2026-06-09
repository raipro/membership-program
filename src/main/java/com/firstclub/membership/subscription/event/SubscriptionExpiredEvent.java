package com.firstclub.membership.subscription.event;

import lombok.Getter;

/**
 * Published when a subscription expires (term ended and auto-renew was off, or renewal
 * was not possible).
 */
@Getter
public class SubscriptionExpiredEvent {

    private final Long subscriptionId;
    private final Long userId;

    public SubscriptionExpiredEvent(Long subscriptionId, Long userId) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
    }
}
