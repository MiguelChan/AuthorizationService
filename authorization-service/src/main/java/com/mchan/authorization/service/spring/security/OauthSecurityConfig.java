package com.mchan.authorization.service.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Keeps confidential-client OAuth authentication stateless and separate from administrator sessions.
 */
@Configuration
public class OauthSecurityConfig {
    /**
     * Delegates confidential-client verification exclusively to the OAuth controller.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauthSecurity(HttpSecurity http) throws Exception {
        return http.securityMatcher("/oauth/**").cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()).build();
    }
}
