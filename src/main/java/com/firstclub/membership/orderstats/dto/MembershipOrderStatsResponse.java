package com.firstclub.membership.orderstats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Current-period order rollup for a user.
 */
@Getter
@AllArgsConstructor
public class MembershipOrderStatsResponse {

    private final Long userId;
    private final String period;
    private final int orderCount;
    private final BigDecimal totalValue;
}
