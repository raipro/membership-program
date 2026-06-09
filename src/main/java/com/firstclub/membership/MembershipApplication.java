package com.firstclub.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the FirstClub Membership Program backend.
 *
 * <p>Scheduling is enabled up front because later phases add scheduled work
 * (subscription expiry sweep + periodic tier re-evaluation — see Task 7).
 */
@SpringBootApplication
@EnableScheduling
public class MembershipApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembershipApplication.class, args);
    }
}
