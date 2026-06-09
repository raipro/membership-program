package com.firstclub.membership.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /** The user's subscription in a given status (used for the active-subscription lookup/guard). */
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    /** Lookup by idempotency key to make subscribe retry-safe. */
    Optional<Subscription> findByIdempotencyKey(String idempotencyKey);
}
