package com.firstclub.membership.benefit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.BenefitMetadataException;
import com.firstclub.membership.benefit.BenefitType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageDiscountBenefitHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PercentageDiscountBenefitHandler handler = new PercentageDiscountBenefitHandler();

    private MetadataAccessor accessor(String json) throws Exception {
        return MetadataAccessor.of(objectMapper.readTree(json));
    }

    @Test
    void supportsPercentageDiscountType() {
        assertThat(handler.supportedType()).isEqualTo(BenefitType.PERCENTAGE_DISCOUNT);
    }

    @Test
    void selectedItemsScope() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"percentage\": 5, \"appliesTo\": \"SELECTED_ITEMS\"}"));

        assertThat(resolved.getSummary()).isEqualTo("5% extra discount on selected items");
        assertThat(resolved.getAttributes())
                .containsEntry("percentage", 5)
                .containsEntry("appliesTo", "SELECTED_ITEMS");
    }

    @Test
    void allItemsScope() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"percentage\": 10, \"appliesTo\": \"ALL_ITEMS\"}"));

        assertThat(resolved.getSummary()).isEqualTo("10% extra discount on all items");
    }

    @Test
    void percentageAbove100_isClampedTo100() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"percentage\": 150, \"appliesTo\": \"ALL_ITEMS\"}"));

        assertThat(resolved.getAttributes()).containsEntry("percentage", 100);
        assertThat(resolved.getSummary()).isEqualTo("100% extra discount on all items");
    }

    @Test
    void negativePercentage_isClampedToZero() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"percentage\": -5, \"appliesTo\": \"ALL_ITEMS\"}"));

        assertThat(resolved.getAttributes()).containsEntry("percentage", 0);
    }

    @Test
    void missingPercentage_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"appliesTo\": \"ALL_ITEMS\"}")))
                .isInstanceOf(BenefitMetadataException.class);
    }

    @Test
    void wrongTypePercentage_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"percentage\": \"abc\", \"appliesTo\": \"ALL_ITEMS\"}")))
                .isInstanceOf(BenefitMetadataException.class);
    }

    @Test
    void invalidScope_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"percentage\": 5, \"appliesTo\": \"EVERYTHING\"}")))
                .isInstanceOf(BenefitMetadataException.class);
    }

    @Test
    void missingScope_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"percentage\": 5}")))
                .isInstanceOf(BenefitMetadataException.class);
    }
}
