package com.firstclub.membership.subscription.event;

import lombok.Getter;

/**
 * Published when a subscription's tier changes via an explicit upgrade or downgrade.
 */
@Getter
public class SubscriptionTierChangedEvent {

    private final Long subscriptionId;
    private final Long userId;
    private final String fromTierCode;
    private final String toTierCode;

    public SubscriptionTierChangedEvent(Long subscriptionId, Long userId, String fromTierCode, String toTierCode) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.fromTierCode = fromTierCode;
        this.toTierCode = toTierCode;
    }
}
