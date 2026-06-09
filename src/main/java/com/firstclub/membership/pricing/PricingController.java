package com.firstclub.membership.pricing;

import com.firstclub.membership.pricing.dto.MembershipPricingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the full purchasable plan × tier pricing matrix.
 *
 * <p>Mapped at {@code /api/v1/plans/pricing}: the literal "pricing" path takes
 * precedence over {@code /api/v1/plans/{id}} on the plan controller, so there is
 * no routing ambiguity.
 */
@RestController
@RequestMapping("/api/v1/plans/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @GetMapping
    public MembershipPricingResponse getPricing() {
        return pricingService.getPricingMatrix();
    }
}
