package com.firstclub.membership.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<MembershipPlan, Long> {

    List<MembershipPlan> findByActiveTrueOrderByDurationDaysAsc();

    Optional<MembershipPlan> findByCode(String code);
}
