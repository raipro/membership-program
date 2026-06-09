package com.firstclub.membership.plan.dto;

import com.firstclub.membership.plan.BillingPeriod;
import com.firstclub.membership.plan.MembershipPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * API view of a {@link MembershipPlan}. A pure data holder — entity-to-DTO
 * mapping lives in {@link com.firstclub.membership.plan.MembershipPlanMapper}.
 */
@Getter
@AllArgsConstructor
public class MembershipPlanResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final BillingPeriod billingPeriod;
    private final int durationDays;
    private final BigDecimal price;
    private final String currency;
    private final boolean active;
}
