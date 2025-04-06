package com.authored.blogapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/health").permitAll()
            .anyRequest().authenticated()           // 🔐 All others protected
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // 🚫 Disable popup auth
            .csrf(csrf -> csrf.disable());               // Disable CSRF for now

        return http.build();
    }
}
