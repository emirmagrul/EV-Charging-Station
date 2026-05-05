package com.ev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Geliştirme aşamasında kolaylık için CSRF kapalı
            .cors(Customizer.withDefaults()) // CorsConfig'deki ayarları kullan
            .authorizeHttpRequests(auth -> auth
                // Misafirlerin görebileceği alanlara izin veriyoruz
                .requestMatchers("/api/stations/**", "/api/map/**", "/api/chargers/**").permitAll()
                // Diğer her şey (Rezervasyon vb.) giriş gerektirecek
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
