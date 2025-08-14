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
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${spring.web.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${spring.web.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${spring.web.cors.allow-credentials}")
    private boolean allowCredentials;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods(methods.toArray(new String[0]))
                .allowedHeaders("*")
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",    
            "http://localhost:5173",    
            "http://127.0.0.1:*"        
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
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
    public BlockchainApplicationConfig blockchainConfig(
            @Value("${blockchain.config.default-difficulty}") int defaultDifficulty,
            @Value("${blockchain.config.genesis-hash}") String genesisHash,
            @Value("${blockchain.config.initial-seed}") String initialSeed,
            @Value("${blockchain.config.mining-reward}") double miningReward) {
        
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