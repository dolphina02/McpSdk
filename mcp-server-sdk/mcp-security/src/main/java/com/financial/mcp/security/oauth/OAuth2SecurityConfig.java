package com.financial.mcp.security.oauth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Production OAuth2 Security Configuration
 * Enables JWT validation.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "false", matchIfMissing = true)
public class OAuth2SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/mcp/rpc", "/api/**").authenticated()
                        .requestMatchers("/actuator/health", "/actuator/metrics").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(null)) // Configure JWT decoder in application.yml
                );

        return http.build();
    }
}
