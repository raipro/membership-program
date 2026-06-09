package com.firstclub.membership.benefit.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.firstclub.membership.benefit.BenefitMetadataException;

import java.util.Set;

/**
 * Strict, typed accessor over a benefit's parsed metadata. Every getter requires the
 * key to be present and of the expected type — a missing key, wrong type, or invalid
 * value all raise {@link BenefitMetadataException} rather than silently defaulting.
 *
 * <p>Centralizing the rules here keeps handlers terse and the failure behaviour uniform.
 */
public final class MetadataAccessor {

    private final JsonNode node;

    private MetadataAccessor(JsonNode node) {
        this.node = node;
    }

    public static MetadataAccessor of(JsonNode node) {
        return new MetadataAccessor(node);
    }

    public int requireInt(String key) {
        JsonNode value = require(key);
        if (!value.isNumber()) {
            throw wrongType(key, "number", value);
        }
        return value.asInt();
    }

    public double requireDouble(String key) {
        JsonNode value = require(key);
        if (!value.isNumber()) {
            throw wrongType(key, "number", value);
        }
        return value.asDouble();
    }

    public String requireText(String key) {
        JsonNode value = require(key);
        if (!value.isTextual()) {
            throw wrongType(key, "string", value);
        }
        return value.asText();
    }

    /** Require a text value that is a member of {@code allowed}. */
    public String requireOneOf(String key, Set<String> allowed) {
        String value = requireText(key);
        if (!allowed.contains(value)) {
            throw new BenefitMetadataException(
                    "metadata key '%s' must be one of %s but was '%s'".formatted(key, allowed, value));
        }
        return value;
    }

    private JsonNode require(String key) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) {
            throw new BenefitMetadataException("required metadata key '%s' is missing".formatted(key));
        }
        return value;
    }

    private static BenefitMetadataException wrongType(String key, String expected, JsonNode actual) {
        return new BenefitMetadataException(
                "metadata key '%s' must be a %s but was %s".formatted(key, expected, actual.getNodeType()));
    }
}
