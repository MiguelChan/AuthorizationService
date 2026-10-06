package com.mchan.authorization.service.spring.security;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Global Spring Security Configuration for the Application.
 * http://localhost:8094/auth-hub/swagger-ui/index.html.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] WEB_ALLOW_LIST =
      {
        "/v2/api-docs",
        "/configuration/ui",
        "/swagger-resources/**",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        "/swagger-ui/**"
      };

    /**
     * .
     *
     * @param web .
     *
     * @throws Exception .
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(WEB_ALLOW_LIST);
    }

    /**
     * .
     *
     * @param http .
     *
     * @throws Exception .
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
        .and()
            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers(WebSecurityConfig::isExplicitStatelessBasic)
        .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/sign-up").permitAll()
            .antMatchers(HttpMethod.GET, "/api/ping", "/api/deep_ping", "/api/csrf").permitAll()
            .antMatchers(HttpMethod.GET, "/", "/login/**", "/register", "/index.html", "/static/**",
                "/favicon.ico", "/manifest.json", "/robots.txt").permitAll()
            .antMatchers("/error").permitAll()
            .anyRequest().authenticated()
        .and()
            .exceptionHandling()
            .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new AntPathRequestMatcher("/api/**"))
        .and()
            .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/api/profile")
            .permitAll()
        .and()
            .logout()
            .permitAll()
        .and()
            .httpBasic();
    }

    /**
     * Allows explicit nonbrowser Basic clients while protecting cookie/session mutations.
     */
    private static boolean isExplicitStatelessBasic(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return request.getHeader("Origin") == null && request.getHeader("Cookie") == null
            && request.getSession(false) == null && authorization != null && authorization.startsWith("Basic ");
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
