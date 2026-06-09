package com.firstclub.membership.benefit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.BenefitMetadataException;
import com.firstclub.membership.benefit.BenefitType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrioritySupportBenefitHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PrioritySupportBenefitHandler handler = new PrioritySupportBenefitHandler();

    private MetadataAccessor accessor(String json) throws Exception {
        return MetadataAccessor.of(objectMapper.readTree(json));
    }

    @Test
    void supportsPrioritySupportType() {
        assertThat(handler.supportedType()).isEqualTo(BenefitType.PRIORITY_SUPPORT);
    }

    @Test
    void resolvesChannelAndSla() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"channel\": \"PHONE\", \"slaHours\": 4}"));

        assertThat(resolved.getSummary()).isEqualTo("Priority support via phone (response SLA 4h)");
        assertThat(resolved.getAttributes())
                .containsEntry("channel", "PHONE")
                .containsEntry("slaHours", 4);
    }

    @Test
    void slaBelowOne_isFlooredToOne() throws Exception {
        ResolvedBenefit resolved = handler.resolve(accessor("{\"channel\": \"EMAIL\", \"slaHours\": 0}"));

        assertThat(resolved.getAttributes()).containsEntry("slaHours", 1);
        assertThat(resolved.getSummary()).isEqualTo("Priority support via email (response SLA 1h)");
    }

    @Test
    void invalidChannel_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"channel\": \"SMS\", \"slaHours\": 4}")))
                .isInstanceOf(BenefitMetadataException.class);
    }

    @Test
    void missingChannel_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"slaHours\": 4}")))
                .isInstanceOf(BenefitMetadataException.class);
    }

    @Test
    void missingSla_isMalformed() {
        assertThatThrownBy(() -> handler.resolve(accessor("{\"channel\": \"PHONE\"}")))
                .isInstanceOf(BenefitMetadataException.class);
    }
}
