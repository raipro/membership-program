package com.firstclub.membership.subscription;

/**
 * Lifecycle states of a subscription.
 *
 * <pre>
 *   (new) --subscribe--> ACTIVE --cancel--> CANCELLED
 *                          |
 *                          +----expire----> EXPIRED
 * </pre>
 *
 * Only {@link #ACTIVE} occupies the one-active-per-user slot; CANCELLED/EXPIRED are
 * terminal. Tier upgrade/downgrade (Task 5) keeps the subscription ACTIVE.
 */
public enum SubscriptionStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED
}
