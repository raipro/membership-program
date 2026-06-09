package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenefitHandlerRegistryTest {

    private final BenefitHandlerRegistry registry =
            new BenefitHandlerRegistry(List.of(new FreeDeliveryBenefitHandler()));

    @Test
    void resolvesRegisteredHandlerByType() {
        assertThat(registry.get(BenefitType.FREE_DELIVERY))
                .isInstanceOf(FreeDeliveryBenefitHandler.class);
    }

    @Test
    void throws_forUnregisteredType() {
        assertThatThrownBy(() -> registry.get(BenefitType.PRIORITY_SUPPORT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No BenefitHandler");
    }
}
