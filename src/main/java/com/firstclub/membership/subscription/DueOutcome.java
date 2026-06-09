package com.firstclub.membership.subscription;

/**
 * Result of processing a due (past-term) subscription during the maintenance sweep.
 */
public enum DueOutcome {
    RENEWED,
    EXPIRED,
    SKIPPED
}
