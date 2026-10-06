package com.mchan.authorization.service.spring;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes the administrator Basic and confidential-client OAuth API contract.
 */
@Configuration
public class OpenApiConfiguration {
    /**
     * Describes explicit client credentials without placing secrets in documentation.
     */
    @Bean
    public OpenAPI authorizationApi() {
        return new OpenAPI().components(new Components().addSecuritySchemes("basic",
            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}
