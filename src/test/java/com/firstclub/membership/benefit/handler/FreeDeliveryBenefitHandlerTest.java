package com.firstclub.membership.benefit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.BenefitMetadataException;
import com.firstclub.membership.benefit.BenefitType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreeDeliveryBenefitHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FreeDeliveryBenefitHandler handler = new FreeDeliveryBenefitHandler();

    private MetadataAccessor accessor(String json) throws Exception {
        return MetadataAccessor.of(objectMapper.readTree(json));
    }

    @Test
    void supportsFreeDeliveryType() {
        assertThat(handler.supportedType()).isEqualTo(BenefitType.FREE_DELIVERY);
    }

    @Test
    void zeroThreshold_meansAllOrders() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"minOrderValue\": 0}"));

        assertThat(resolved.getSummary()).isEqualTo("Free delivery on all eligible orders");
        assertThat(resolved.getAttributes())
                .containsEntry("minOrderValue", 0.0)
                .containsEntry("eligibleOrdersOnly", false);
    }

    @Test
    void positiveThreshold_appearsInSummaryAndFlag() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"minOrderValue\": 500}"));

        assertThat(resolved.getSummary()).isEqualTo("Free delivery on orders above 500");
        assertThat(resolved.getAttributes())
                .containsEntry("minOrderValue", 500.0)
                .containsEntry("eligibleOrdersOnly", true);
    }

    @Test
    void missingThreshold_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{}")))
                .isInstanceOf(BenefitMetadataException.class);
    }
}
