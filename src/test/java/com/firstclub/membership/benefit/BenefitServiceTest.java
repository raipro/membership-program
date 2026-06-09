package com.firstclub.membership.benefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.benefit.dto.MembershipBenefitResponse;
import com.firstclub.membership.benefit.handler.BenefitHandler;
import com.firstclub.membership.benefit.handler.BenefitHandlerRegistry;
import com.firstclub.membership.benefit.handler.MetadataAccessor;
import com.firstclub.membership.benefit.handler.ResolvedBenefit;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.tier.TierRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link BenefitService} orchestration: tier-existence guard, empty
 * result, handler dispatch, and malformed-metadata handling.
 */
class BenefitServiceTest {

    private final TierBenefitRepository tierBenefitRepository = mock(TierBenefitRepository.class);
    private final TierRepository tierRepository = mock(TierRepository.class);
    private final BenefitHandlerRegistry handlerRegistry = mock(BenefitHandlerRegistry.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BenefitService service = new BenefitService(
            tierBenefitRepository, tierRepository, handlerRegistry, objectMapper);

    @Test
    void unknownTier_throwsNotFound() {
        when(tierRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getBenefitsForTier(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void tierWithNoActiveBenefits_returnsEmptyList() {
        when(tierRepository.existsById(1L)).thenReturn(true);
        when(tierBenefitRepository.findActiveByTierId(1L)).thenReturn(List.of());

        assertThat(service.getBenefitsForTier(1L)).isEmpty();
    }

    @Test
    void dispatchesToHandler_andAssemblesResponse() {
        when(tierRepository.existsById(1L)).thenReturn(true);

        Benefit benefit = mock(Benefit.class);
        when(benefit.getCode()).thenReturn("FREE_DELIVERY");
        when(benefit.getType()).thenReturn(BenefitType.FREE_DELIVERY);
        when(benefit.getDescription()).thenReturn("Free delivery on eligible orders");

        TierBenefit link = mock(TierBenefit.class);
        when(link.getBenefit()).thenReturn(benefit);
        when(link.getBenefitMetadata()).thenReturn("{\"minOrderValue\": 0}");
        when(tierBenefitRepository.findActiveByTierId(1L)).thenReturn(List.of(link));

        BenefitHandler benefitHandler = mock(BenefitHandler.class);
        when(handlerRegistry.get(BenefitType.FREE_DELIVERY)).thenReturn(benefitHandler);
        when(benefitHandler.resolve(any(MetadataAccessor.class)))
                .thenReturn(new ResolvedBenefit("Free delivery on all eligible orders",
                        Map.of("minOrderValue", 0.0)));

        List<MembershipBenefitResponse> result = service.getBenefitsForTier(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("FREE_DELIVERY");
        assertThat(result.get(0).getSummary()).isEqualTo("Free delivery on all eligible orders");
        assertThat(result.get(0).getAttributes()).containsEntry("minOrderValue", 0.0);
    }

    @Test
    void malformedMetadataJson_isRejected() {
        when(tierRepository.existsById(1L)).thenReturn(true);

        TierBenefit link = mock(TierBenefit.class);
        when(link.getBenefit()).thenReturn(mock(Benefit.class));
        when(link.getBenefitMetadata()).thenReturn("{not valid json");
        when(tierBenefitRepository.findActiveByTierId(1L)).thenReturn(List.of(link));

        assertThatThrownBy(() -> service.getBenefitsForTier(1L))
                .isInstanceOf(BenefitMetadataException.class);
    }
}
