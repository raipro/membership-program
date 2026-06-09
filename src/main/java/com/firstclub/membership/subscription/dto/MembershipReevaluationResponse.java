package com.firstclub.membership.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Summary of a periodic tier re-evaluation run.
 */
@Getter
@AllArgsConstructor
public class MembershipReevaluationResponse {

    private final int usersEvaluated;
}
