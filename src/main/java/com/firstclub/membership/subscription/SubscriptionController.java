package com.firstclub.membership.subscription;

import com.firstclub.membership.subscription.dto.MembershipSubscriptionRequest;
import com.firstclub.membership.subscription.dto.MembershipSubscriptionResponse;
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
 * Subscribe to a plan+tier and look up a user's current membership.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipSubscriptionResponse subscribe(@Valid @RequestBody MembershipSubscriptionRequest request) {
        return subscriptionService.subscribe(request);
    }

    @GetMapping("/users/{userId}/subscription")
    public MembershipSubscriptionResponse getCurrentSubscription(@PathVariable Long userId) {
        return subscriptionService.getCurrentSubscription(userId);
    }
}
