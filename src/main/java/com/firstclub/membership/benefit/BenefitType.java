package com.firstclub.membership.benefit;

/**
 * The kinds of perk a membership tier can grant. Each type has a dedicated
 * {@link com.firstclub.membership.benefit.handler.BenefitHandler} that interprets
 * the per-tier JSON config. Adding a new perk = new enum value + new handler,
 * with no changes to existing code.
 */
public enum BenefitType {
    /** Free delivery, optionally above a minimum order value. */
    FREE_DELIVERY,
    /** Extra % discount on selected items/categories. */
    PERCENTAGE_DISCOUNT,
    /** Early access to sales and access to exclusive deals. */
    EARLY_ACCESS,
    /** Priority customer support (channel + SLA). */
    PRIORITY_SUPPORT
}
