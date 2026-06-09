package com.firstclub.membership.benefit.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.BenefitMetadataException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the strict metadata accessor — the single place every malformed-metadata
 * rule lives. Covers present/missing/wrong-type for each accessor and enum membership.
 */
class MetadataAccessorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MetadataAccessor accessor(String json) {
        try {
            return MetadataAccessor.of(objectMapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    void requireInt_returnsValue_whenPresentAndNumeric() {
        assertThat(accessor("{\"n\": 7}").requireInt("n")).isEqualTo(7);
    }

    @Test
    void requireInt_throws_whenMissing() {
        assertThatThrownBy(() -> accessor("{}").requireInt("n"))
                .isInstanceOf(BenefitMetadataException.class)
                .hasMessageContaining("'n' is missing");
    }

    @Test
    void requireInt_throws_whenWrongType() {
        assertThatThrownBy(() -> accessor("{\"n\": \"abc\"}").requireInt("n"))
                .isInstanceOf(BenefitMetadataException.class)
                .hasMessageContaining("must be a number");
    }

    @Test
    void requireDouble_returnsValue_whenNumeric() {
        assertThat(accessor("{\"d\": 12.5}").requireDouble("d")).isEqualTo(12.5);
    }

    @Test
    void requireDouble_throws_whenWrongType() {
        assertThatThrownBy(() -> accessor("{\"d\": true}").requireDouble("d"))
                .isInstanceOf(BenefitMetadataException.class)
                .hasMessageContaining("must be a number");
    }

    @Test
    void requireText_returnsValue_whenTextual() {
        assertThat(accessor("{\"s\": \"hello\"}").requireText("s")).isEqualTo("hello");
    }

    @Test
    void requireText_throws_whenWrongType() {
        assertThatThrownBy(() -> accessor("{\"s\": 5}").requireText("s"))
                .isInstanceOf(BenefitMetadataException.class)
                .hasMessageContaining("must be a string");
    }

    @Test
    void requireOneOf_returnsValue_whenAllowed() {
        assertThat(accessor("{\"c\": \"PHONE\"}").requireOneOf("c", Set.of("EMAIL", "PHONE")))
                .isEqualTo("PHONE");
    }

    @Test
    void requireOneOf_throws_whenNotInAllowedSet() {
        assertThatThrownBy(() -> accessor("{\"c\": \"SMS\"}").requireOneOf("c", Set.of("EMAIL", "PHONE")))
                .isInstanceOf(BenefitMetadataException.class)
                .hasMessageContaining("must be one of");
    }

    @Test
    void requireOneOf_throws_whenMissing() {
        assertThatThrownBy(() -> accessor("{}").requireOneOf("c", Set.of("EMAIL")))
                .isInstanceOf(BenefitMetadataException.class)
                .hasMessageContaining("'c' is missing");
    }
}
