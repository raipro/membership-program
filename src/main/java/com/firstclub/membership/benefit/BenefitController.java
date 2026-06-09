package com.firstclub.membership.benefit;

import com.firstclub.membership.benefit.dto.MembershipBenefitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes the effective, configured benefits for a tier.
 */
@RestController
@RequestMapping("/api/v1/tiers/{tierId}/benefits")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping
    public List<MembershipBenefitResponse> getBenefits(@PathVariable Long tierId) {
        return benefitService.getBenefitsForTier(tierId);
    }
}
