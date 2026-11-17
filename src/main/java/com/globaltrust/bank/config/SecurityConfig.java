package com.globaltrust.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Authentication = HTTP Basic over a JPA user store with BCrypt hashing.
 * Authorization = method-level RBAC via {@code @PreAuthorize} on the services
 * (enabled here with {@code @EnableMethodSecurity}).
 *
 * Swagger UI, the OpenAPI docs and the H2 console are left open so the
 * prototype can be explored and demonstrated without friction.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Stateless JSON API: CSRF tokens are not used; the H2 console needs framing.
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                        "/h2-console/**").permitAll()
                .anyRequest().authenticated())
            .httpBasic(basic -> {});
        return http.build();
    }
}
