package com.firstclub.membership.tierevaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TierCriterionRepository extends JpaRepository<TierCriterion, Long> {

    List<TierCriterion> findByTierId(Long tierId);
}
