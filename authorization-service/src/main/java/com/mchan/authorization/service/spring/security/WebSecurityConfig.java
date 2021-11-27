package com.mchan.authorization.service.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/sign-up").permitAll()
            .antMatchers("/api/ping*", "/api/deep_ping*").permitAll()
            .antMatchers("/*", "/**", "/*/**").permitAll()
            .anyRequest().authenticated()
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
     * Configurer for CORS Methods.
     *
     * @return .
     */
    @Bean
    public WebMvcConfigurer corsConfigure() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/**")
                    .allowCredentials(true)
                    .allowedMethods("*")
                    .allowedOriginPatterns("*");
            }
        };
    }
}
