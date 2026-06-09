package com.firstclub.membership.tier.dto;

import com.firstclub.membership.tier.MembershipTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API view of a {@link MembershipTier}. A pure data holder; mapping lives in
 * {@link com.firstclub.membership.common.mapper.MembershipMapper}.
 *
 * <p>Associated benefits are layered onto the tier view in Task 3.
 */
@Getter
@AllArgsConstructor
public class MembershipTierResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final int rank;
    private final String description;
    private final boolean active;
}
