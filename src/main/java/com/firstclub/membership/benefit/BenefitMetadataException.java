package com.firstclub.membership.benefit;

/**
 * Raised when a tier's benefit metadata is malformed: unparseable JSON, a required
 * key absent, or a key present with the wrong type/invalid value.
 *
 * <p>This is an operator/configuration error (metadata is seeded/admin-managed, not
 * user input). The detailed message is logged server-side for diagnosis; the client
 * receives a generic 500 (see {@code GlobalExceptionHandler}).
 */
public class BenefitMetadataException extends RuntimeException {

    public BenefitMetadataException(String message) {
        super(message);
    }

    public BenefitMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
