package com.firstclub.membership.orderstats;

import com.firstclub.membership.orderstats.dto.MembershipOrderRequest;
import com.firstclub.membership.orderstats.dto.MembershipOrderStatsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Records simulated order signals and exposes the current-period rollup.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/orders")
@RequiredArgsConstructor
public class OrderStatsController {

    private final OrderStatsService orderStatsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipOrderStatsResponse recordOrder(@PathVariable Long userId,
                                                    @Valid @RequestBody MembershipOrderRequest request) {
        return orderStatsService.recordOrder(userId, request.getAmount());
    }

    @GetMapping
    public MembershipOrderStatsResponse getStats(@PathVariable Long userId) {
        return orderStatsService.getCurrentStats(userId);
    }
}
