package com.firstclub.membership.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /** The user's subscription in a given status (used for the active-subscription lookup/guard). */
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    /** Lookup by idempotency key to make subscribe retry-safe. */
    Optional<Subscription> findByIdempotencyKey(String idempotencyKey);

    /** Ids of subscriptions whose term has ended, for the maintenance sweep. */
    @Query("select s.id from Subscription s where s.status = :status and s.endDate < :asOf")
    List<Long> findDueSubscriptionIds(@Param("status") SubscriptionStatus status,
                                      @Param("asOf") LocalDate asOf);
}
