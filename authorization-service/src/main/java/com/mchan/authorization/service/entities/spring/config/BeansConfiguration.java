package com.mchan.authorization.service.entities.spring.config;

import com.mchan.authorization.lib.models.AuthenticationType;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import com.mchan.authorization.service.entities.authentication.strategies.impl.ClassicAuthenticationStrategy;
import java.util.Map;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the beans Configuration.
 */
@Configuration
public class BeansConfiguration {

    /**
     * Gets the email validator.
     *
     * @return .
     */
    @Bean
    public EmailValidator provideEmailValidator() {
        return EmailValidator.getInstance(false);
    }

    @Bean("authTypeToStrategyMap")
    public Map<AuthenticationType, AuthenticationStrategy> provideAuthTypeToAuthStratMap(
        ClassicAuthenticationStrategy classicAuthenticationStrategy
    ) {
        return Map.of(AuthenticationType.CLASSIC, classicAuthenticationStrategy);
    }



}
