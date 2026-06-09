package com.firstclub.membership.orderstats.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Simulated order signal: a single order of {@code amount} for the user, folded into
 * the current period's rollup.
 */
@Getter
@Setter
@NoArgsConstructor
public class MembershipOrderRequest {

    @NotNull
    @Positive
    private BigDecimal amount;
}
