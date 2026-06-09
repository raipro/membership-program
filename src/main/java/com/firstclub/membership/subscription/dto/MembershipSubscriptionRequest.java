package com.firstclub.membership.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subscribe request: a user picks a (plan, tier) pair. {@code autoRenew} defaults to
 * true when omitted; {@code idempotencyKey} is optional and makes retries safe.
 *
 * <p>Mutable with setters so Jackson can deserialize the request body.
 */
@Getter
@Setter
@NoArgsConstructor
public class MembershipSubscriptionRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long planId;

    @NotNull
    private Long tierId;

    private Boolean autoRenew;

    private String idempotencyKey;
}
