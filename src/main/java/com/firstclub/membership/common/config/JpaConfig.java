package com.firstclub.membership.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate}/{@code @LastModifiedDate}
 * fields on {@link com.firstclub.membership.common.domain.AuditEntity} are populated
 * automatically on persist/update.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
