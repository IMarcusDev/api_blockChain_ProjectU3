package com.proyecto.mjcd_software.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${blockchain.config.default-difficulty}")
    private int defaultDifficulty;

    @Value("${blockchain.config.genesis-hash}")
    private String genesisHash;

    @Value("${blockchain.config.initial-seed}")
    private String initialSeed;

    @Value("${blockchain.config.mining-reward}")
    private double miningReward;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000", 
                    "http://localhost:5173", 
                    "http://127.0.0.1:3000", 
                    "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true) // Necesario para JWT
                .maxAge(3600);
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
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Headers permitidos - ESPECÍFICOS en lugar de "*"
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Cache-Control"
        ));
        
        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));

        // Permitir credenciales (necesario para JWT en headers)
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public BlockchainApplicationConfig blockchainConfig() {
        return new BlockchainApplicationConfig(defaultDifficulty, genesisHash, initialSeed, miningReward);
    }
}

class BlockchainApplicationConfig {
    private final int defaultDifficulty;
    private final String genesisHash;
    private final String initialSeed;
    private final double miningReward;

    public BlockchainApplicationConfig(int defaultDifficulty, String genesisHash, String initialSeed, double miningReward) {
        this.defaultDifficulty = defaultDifficulty;
        this.genesisHash = genesisHash;
        this.initialSeed = initialSeed;
        this.miningReward = miningReward;
    }

    public int getDefaultDifficulty() { return defaultDifficulty; }
    public String getGenesisHash() { return genesisHash; }
    public String getInitialSeed() { return initialSeed; }
    public double getMiningReward() { return miningReward; }
}