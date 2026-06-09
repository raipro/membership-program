package com.firstclub.membership.benefit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.BenefitMetadataException;
import com.firstclub.membership.benefit.BenefitType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EarlyAccessBenefitHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EarlyAccessBenefitHandler handler = new EarlyAccessBenefitHandler();

    private MetadataAccessor accessor(String json) throws Exception {
        return MetadataAccessor.of(objectMapper.readTree(json));
    }

    @Test
    void supportsEarlyAccessType() {
        assertThat(handler.supportedType()).isEqualTo(BenefitType.EARLY_ACCESS);
    }

    @Test
    void positiveHours_includeEarlyAccessInSummary() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"earlyAccessHours\": 24}"));

        assertThat(resolved.getSummary()).isEqualTo("24h early access to sales + exclusive deals");
        assertThat(resolved.getAttributes())
                .containsEntry("earlyAccessHours", 24)
                .containsEntry("exclusiveDeals", true);
    }

    @Test
    void zeroHours_meansExclusiveDealsOnly() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"earlyAccessHours\": 0}"));

        assertThat(resolved.getSummary()).isEqualTo("Access to exclusive deals");
        assertThat(resolved.getAttributes()).containsEntry("earlyAccessHours", 0);
    }

    @Test
    void missingHours_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{}")))
                .isInstanceOf(BenefitMetadataException.class);
    }
}
