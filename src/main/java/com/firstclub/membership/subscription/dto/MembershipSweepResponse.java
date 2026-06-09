package com.firstclub.membership.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Summary of an expiry/renewal sweep run.
 */
@Getter
@AllArgsConstructor
public class MembershipSweepResponse {

    private final int processed;
    private final int renewed;
    private final int expired;
}
