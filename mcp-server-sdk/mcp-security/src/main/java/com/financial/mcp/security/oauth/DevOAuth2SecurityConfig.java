package com.financial.mcp.security.oauth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development OAuth2 Security Configuration
 * Bypasses JWT validation and permits all requests.
 * DO NOT use in production!
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "true")
public class DevOAuth2SecurityConfig {

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .csrf().disable()
                .httpBasic().disable();

        return http.build();
    }
}
