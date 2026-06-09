package com.firstclub.membership.tierevaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Result of tier evaluation: the highest tier the user is currently eligible for
 * (the authorization ceiling), plus the inputs it was computed from. {@code evaluatedAt}
 * is null if the user has not been evaluated yet (default = base tier).
 */
@Getter
@AllArgsConstructor
public class MembershipEligibilityResponse {

    private final Long userId;
    private final Long eligibleTierId;
    private final String eligibleTierCode;
    private final String eligibleTierName;
    private final int eligibleTierRank;
    private final Instant evaluatedAt;
    private final int orderCount;
    private final BigDecimal totalSpend;
    private final String cohort;
}
