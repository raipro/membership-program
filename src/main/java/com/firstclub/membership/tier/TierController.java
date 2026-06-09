package com.firstclub.membership.tier;

import com.firstclub.membership.tier.dto.MembershipTierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read APIs for the membership tier catalog.
 */
@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
public class TierController {

    private final TierService tierService;

    @GetMapping
    public List<MembershipTierResponse> getTiers() {
        return tierService.getActiveTiers();
    }

    @GetMapping("/{id}")
    public MembershipTierResponse getTier(@PathVariable Long id) {
        return tierService.getTier(id);
    }
}
