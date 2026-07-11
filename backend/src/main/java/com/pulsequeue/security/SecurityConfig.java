package com.pulsequeue.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Enforcement for the protected paths happens inside {@link ApiKeyAuthFilter}
     * itself (it writes 401 directly and short-circuits the chain), so the
     * filter chain here just permits everything and adds that filter —
     * there's no {@code UserDetailsService}, form login, or session to speak
     * of, since this is a single shared-secret header, not user auth.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyProperties properties, ObjectMapper objectMapper)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new ApiKeyAuthFilter(properties, objectMapper), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
