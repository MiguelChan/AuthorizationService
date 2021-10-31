package com.mchan.authorization.service.entities.spring.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Jackson Modules.
 */
@Configuration
public class ModuleConfiguration {

    /**
     * .
     *
     * @return .
     */
    @Bean
    public Module javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        return module;
    }

}
