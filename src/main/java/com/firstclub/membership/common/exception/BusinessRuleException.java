package com.firstclub.membership.common.exception;

/**
 * Thrown when a request is well-formed but violates a domain rule
 * (e.g. subscribing to an inactive plan, or an unpriced plan+tier combo).
 * Maps to HTTP 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
