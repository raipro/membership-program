package com.firstclub.membership.subscription.event;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Published when a subscription is renewed for a new term (possibly at a downgraded tier).
 */
@Getter
public class SubscriptionRenewedEvent {

    private final Long subscriptionId;
    private final Long userId;
    private final String tierCode;
    private final LocalDate newEndDate;

    public SubscriptionRenewedEvent(Long subscriptionId, Long userId, String tierCode, LocalDate newEndDate) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.tierCode = tierCode;
        this.newEndDate = newEndDate;
    }
}
