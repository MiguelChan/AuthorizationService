package com.mchan.authorization.service.spring.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Global Spring Security Configuration for the Application.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

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
            .antMatchers(HttpMethod.POST, "/sign-up").permitAll()
            .antMatchers("/ping*", "/deep_ping*").permitAll()
            .anyRequest().authenticated()
        .and()
            .formLogin()
            .defaultSuccessUrl("/profile")
            .permitAll()
        .and()
            .logout()
            .permitAll()
        .and()
            .httpBasic();
    }
}
