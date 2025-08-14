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

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz

                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/blockchain/chain").permitAll()
                .requestMatchers("/validation/**").permitAll()
                .requestMatchers("/block/*/validate").permitAll()
                .requestMatchers("/blockchain/stats").permitAll()
                .requestMatchers("/blockchain/list").permitAll()
                .requestMatchers("/users/points").permitAll()
                .requestMatchers("/users/stats").permitAll()
                .requestMatchers("/config").permitAll()
                .requestMatchers("/file/supported-types").permitAll()
                
                .requestMatchers("/blockchain/create").authenticated()
                .requestMatchers("/blockchain/block/**").authenticated()
                .requestMatchers("/block/**").authenticated()
                .requestMatchers("/file/**").authenticated()
                .requestMatchers("/users/**").authenticated()
                .requestMatchers("/config/**").authenticated()
                .requestMatchers("/validation/repair").authenticated()
                
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}