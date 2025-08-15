package com.proyecto.mjcd_software.config;

import com.proyecto.mjcd_software.security.AuthenticationFilter;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private AuthenticationFilter authenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("OPTIONS", "/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/blockchain/chain").permitAll()
                .requestMatchers("/api/v1/blockchain/stats").permitAll()
                .requestMatchers("/api/v1/blockchain/list").permitAll()
                .requestMatchers("/api/v1/validation/**").permitAll()
                .requestMatchers("/api/v1/config").permitAll()
                .requestMatchers("/api/v1/users/points").permitAll()
                .requestMatchers("/api/v1/users/stats").permitAll()
                .requestMatchers("/api/v1/file/supported-types").permitAll()

                .requestMatchers("/api/v1/mining/**").authenticated()
                .requestMatchers("/api/v1/file/upload").authenticated()
                .requestMatchers("/api/v1/blockchain/create").authenticated()
                .requestMatchers("/api/v1/blockchain/block/**").authenticated()
                .requestMatchers("/api/v1/config/difficulty").authenticated()
                .requestMatchers("/api/v1/config/reset").authenticated()
                .requestMatchers("/api/v1/users/generate-random").authenticated()
                .requestMatchers("/api/v1/users/clear").authenticated()
                .requestMatchers("/api/v1/validation/repair").authenticated()

                .anyRequest().authenticated()
            )

            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}