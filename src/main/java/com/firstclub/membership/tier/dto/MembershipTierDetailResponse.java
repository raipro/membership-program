package com.firstclub.membership.tier.dto;

import com.firstclub.membership.benefit.dto.MembershipBenefitResponse;
import com.firstclub.membership.tier.MembershipTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Single-tier view with its resolved benefits embedded. The list endpoint stays lean
 * ({@link MembershipTierResponse}); only the detail view carries benefits.
 */
@Getter
@AllArgsConstructor
public class MembershipTierDetailResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final int rank;
    private final String description;
    private final boolean active;
    private final List<MembershipBenefitResponse> benefits;
}
