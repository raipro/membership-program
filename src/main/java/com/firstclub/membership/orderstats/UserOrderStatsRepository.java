package com.firstclub.membership.orderstats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOrderStatsRepository extends JpaRepository<UserOrderStats, Long> {

    Optional<UserOrderStats> findByUserIdAndPeriod(Long userId, String period);
}
