package com.proyecto.mjcd_software.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${blockchain.config.default-difficulty:4}")
    private int defaultDifficulty;

    @Value("${blockchain.config.genesis-hash:0000000000000000000}")
    private String genesisHash;

    @Value("${blockchain.config.initial-seed:ESPE}")
    private String initialSeed;

    @Value("${blockchain.config.mining-reward:1.0}")
    private double miningReward;

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public BlockchainConfig blockchainConfig() {
        return new BlockchainConfig(defaultDifficulty, genesisHash, initialSeed, miningReward);
    }
}

class BlockchainConfig {
    private final int defaultDifficulty;
    private final String genesisHash;
    private final String initialSeed;
    private final double miningReward;

    public BlockchainConfig(int defaultDifficulty, String genesisHash, String initialSeed, double miningReward) {
        this.defaultDifficulty = defaultDifficulty;
        this.genesisHash = genesisHash;
        this.initialSeed = initialSeed;
        this.miningReward = miningReward;
    }

    public int getDefaultDifficulty() { 
        return defaultDifficulty; 
    }
    
    public String getGenesisHash() { 
        return genesisHash; 
    }
    
    public String getInitialSeed() { 
        return initialSeed; 
    }
    
    public double getMiningReward() { 
        return miningReward; 
    }
}