package com.mchan.authorization.service.entities.spring.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringConfig that holds all the EnvVars Configuration.
 */
@Configuration
public class EnvVarsConfiguration {

    @Value("${app.pepper.value}")
    private String pepperValue;
    @Value("${app.bcrypt.iterations}")
    private int bcryptIterations;

    @Qualifier("pepperValue")
    @Bean(name = "pepperValue")
    public String providesPepperValue() {
        return pepperValue;
    }

    @Qualifier("bcryptIterations")
    @Bean(name = "bcryptIterations")
    public int provideBcryptIterations() {
        return bcryptIterations;
    }

}
