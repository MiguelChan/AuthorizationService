package com.mchan.authorization.service.entities.spring.config;

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

}
