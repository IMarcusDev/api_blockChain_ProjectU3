package com.proyecto.mjcd_software.config;

import com.proyecto.mjcd_software.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Permitir preflight requests
                .requestMatchers("OPTIONS", "/**").permitAll()
                
                // Endpoints públicos (sin autenticación)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/blockchain/chain").permitAll()
                .requestMatchers("/validation/blockchain").permitAll()
                .requestMatchers("/validation/block/**").permitAll()
                .requestMatchers("/validation/integrity").permitAll()
                .requestMatchers("/validation/simulate").permitAll()
                .requestMatchers("/block/*/validate").permitAll()
                .requestMatchers("/blockchain/stats").permitAll()
                .requestMatchers("/blockchain/list").permitAll()
                .requestMatchers("/users/points").permitAll()
                .requestMatchers("/users/stats").permitAll()
                .requestMatchers("/config").permitAll()
                .requestMatchers("/file/supported-types").permitAll()
                
                // Endpoints que requieren autenticación
                .requestMatchers("/blockchain/create").authenticated()
                .requestMatchers("/blockchain/block/**").authenticated()
                .requestMatchers("/block/**").authenticated()
                .requestMatchers("/file/**").authenticated()
                .requestMatchers("/users/generate-random").authenticated()
                .requestMatchers("/users/clear").authenticated()
                .requestMatchers("/users/points/**").authenticated() // Para POST/PUT/DELETE
                .requestMatchers("/config/difficulty").authenticated()
                .requestMatchers("/config/reset").authenticated()
                .requestMatchers("/validation/repair").authenticated()
                
                // Por defecto, todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}