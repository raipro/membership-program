package com.firstclub.membership.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to change a subscription's tier (upgrade or downgrade): the target tier id.
 */
@Getter
@Setter
@NoArgsConstructor
public class MembershipTierChangeRequest {

    @NotNull
    private Long tierId;
}
