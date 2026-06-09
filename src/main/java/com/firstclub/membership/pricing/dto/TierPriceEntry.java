package com.firstclub.membership.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * One priceable tier within a plan, in the pricing matrix.
 */
@Getter
@AllArgsConstructor
public class TierPriceEntry {

    private final Long tierId;
    private final String code;
    private final String name;
    private final int rank;
    private final BigDecimal price;
}
