package com.firstclub.membership.tierevaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTierStatusRepository extends JpaRepository<UserTierStatus, Long> {

    Optional<UserTierStatus> findByUserId(Long userId);
}
