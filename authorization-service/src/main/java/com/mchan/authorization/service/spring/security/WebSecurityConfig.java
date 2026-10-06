package com.mchan.authorization.service.spring.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Global Spring Security Configuration for the Application.
 * http://localhost:8094/auth-hub/swagger-ui/index.html.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String[] WEB_ALLOW_LIST =
      {
        "/v3/api-docs/**",
        "/configuration/ui",
        "/swagger-resources/**",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        "/swagger-ui/**"
      };

    /**
     * Protects administrator browser sessions and explicit stateless Basic clients.
     */
    @Bean
    public org.springframework.security.web.SecurityFilterChain applicationSecurity(HttpSecurity http,
                                                                                   EntitiesAuthenticationProvider provider, RequestAdmissionFilter admission) throws Exception {
        http.addFilterBefore(admission, org.springframework.security.web.csrf.CsrfFilter.class).cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(WebSecurityConfig::isExplicitStatelessBasic))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(WEB_ALLOW_LIST).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/sign-up").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/ping", "/api/deep_ping", "/api/csrf").permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/login/**", "/register", "/index.html", "/static/**", "/favicon.ico",
                    "/manifest.json", "/robots.txt").permitAll()
                .requestMatchers("/error").permitAll().anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), request -> request.getRequestURI().startsWith("/api/")))
            .authenticationProvider(provider)
            .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/api/profile").permitAll())
            .logout(logout -> logout.permitAll())
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }

    /**
     * Allows explicit nonbrowser Basic clients while protecting cookie/session mutations.
     */
    private static boolean isExplicitStatelessBasic(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return request.getHeader("Origin") == null && request.getHeader("Cookie") == null
            && request.getSession(false) == null && authorization != null && authorization.regionMatches(true, 0, "Basic ", 0, 6);
    }

    /**
     * Restricts credentialed browser access to explicitly configured exact origins.
     */
    @Bean
    public static CorsConfigurationSource corsConfigurationSource(@Value("${app.security.allowed-origins:}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowed = Arrays.asList(origins.split(","));
        for (String origin : allowed) {
            if (origin.contains("*")) {
                throw new IllegalArgumentException("Credentialed CORS requires exact origins");
            }
        }
        config.setAllowedOrigins(allowed);
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-CSRF-TOKEN"));
        config.setMaxAge(600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
