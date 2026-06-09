package com.firstclub.membership.common.exception;

import com.firstclub.membership.benefit.BenefitMetadataException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the exception → HTTP mapping. Covers the branches not exercised by the
 * integration suite: benefit-metadata → generic 500, optimistic-lock → 409, and the
 * not-found / business-rule / bad-request mappings.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        return request;
    }

    @Test
    void notFound_mapsTo404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Tier with id '9' not found"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error()).isEqualTo("NOT_FOUND");
    }

    @Test
    void businessRule_mapsTo409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusinessRule(new BusinessRuleException("already active"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    @Test
    void illegalArgument_mapsTo400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void optimisticLock_mapsTo409ConcurrentModification() {
        ResponseEntity<ErrorResponse> response =
                handler.handleOptimisticLock(new OptimisticLockingFailureException("stale"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error()).isEqualTo("CONCURRENT_MODIFICATION");
    }

    @Test
    void benefitMetadata_mapsToGeneric500_withoutLeakingDetail() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBenefitMetadata(new BenefitMetadataException("key 'percentage' missing"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_ERROR");
        // The precise cause is logged, not returned to the client.
        assertThat(response.getBody().message()).doesNotContain("percentage");
    }

    @Test
    void unexpected_mapsToGeneric500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("boom"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_ERROR");
    }
}
