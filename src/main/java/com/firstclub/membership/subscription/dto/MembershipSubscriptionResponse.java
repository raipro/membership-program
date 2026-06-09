package com.firstclub.membership.subscription.dto;

import com.firstclub.membership.plan.BillingPeriod;
import com.firstclub.membership.subscription.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * API view of a subscription, including current status, expiry, and the snapshotted
 * price. {@code daysRemaining} is computed against the injected clock.
 */
@Getter
@AllArgsConstructor
public class MembershipSubscriptionResponse {

    private final Long id;
    private final Long userId;
    private final String planCode;
    private final String planName;
    private final BillingPeriod billingPeriod;
    private final String tierCode;
    private final String tierName;
    private final SubscriptionStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long daysRemaining;
    private final BigDecimal price;
    private final String currency;
    private final boolean autoRenew;
}
