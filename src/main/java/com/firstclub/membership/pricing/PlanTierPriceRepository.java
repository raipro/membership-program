package com.firstclub.membership.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanTierPriceRepository extends JpaRepository<PlanTierPrice, Long> {

    /**
     * All purchasable price points (price + plan + tier all active), with plan and tier
     * fetch-joined to avoid N+1 when assembling the pricing matrix.
     */
    @Query("""
            select p from PlanTierPrice p
            join fetch p.plan pl
            join fetch p.tier t
            where p.active = true and pl.active = true and t.active = true
            order by pl.durationDays asc, t.rank asc
            """)
    List<PlanTierPrice> findPurchasableMatrix();

    /** Resolve the price for a specific plan+tier pair (used at subscribe time, Task 4). */
    Optional<PlanTierPrice> findByPlanIdAndTierIdAndActiveTrue(Long planId, Long tierId);
}
