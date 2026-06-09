package com.firstclub.membership.benefit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TierBenefitRepository extends JpaRepository<TierBenefit, Long> {

    /**
     * Active benefits configured for a tier, with the benefit fetch-joined to avoid
     * N+1 when resolving. Ordered by benefit id for stable output.
     */
    @Query("""
            select tb from TierBenefit tb
            join fetch tb.benefit b
            where tb.tier.id = :tierId and b.active = true
            order by b.id asc
            """)
    List<TierBenefit> findActiveByTierId(@Param("tierId") Long tierId);
}
