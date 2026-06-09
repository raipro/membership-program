package com.firstclub.membership;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the Spring application context loads with the default (H2) profile.
 */
@SpringBootTest
class MembershipApplicationTests {

    @Test
    void contextLoads() {
        // Context startup is the assertion; fails the build if any bean wiring is broken.
    }
}
