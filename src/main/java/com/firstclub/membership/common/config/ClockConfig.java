package com.firstclub.membership.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a {@link Clock} bean so time-dependent logic (subscription start/expiry)
 * depends on an injectable clock rather than calling {@code LocalDate.now()} directly.
 * Tests can supply a fixed clock to make expiry behaviour deterministic.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
