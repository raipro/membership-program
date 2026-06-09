package com.firstclub.membership.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the generated Swagger UI ({@code /swagger-ui.html}) and
 * spec ({@code /v3/api-docs}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI membershipOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FirstClub Membership Program API")
                .description("Subscription-based memberships with tiered, configurable benefits and "
                        + "criteria-driven tier eligibility.")
                .version("0.0.1"));
    }
}
