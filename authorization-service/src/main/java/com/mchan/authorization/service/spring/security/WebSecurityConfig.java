package com.mchan.authorization.service.spring.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
}
