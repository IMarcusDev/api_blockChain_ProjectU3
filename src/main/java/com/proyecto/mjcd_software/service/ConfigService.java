package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    
    @Value("${blockchain.config.default-difficulty:4}")
    private int defaultDifficulty;
    
    @Value("${blockchain.config.initial-seed:ESPE}")
    private String initialSeed;
    
    @Value("${blockchain.config.mining-reward:1.0}")
    private double miningReward;
    
    @Value("${blockchain.config.genesis-hash:0000000000000000000}")
    private String genesisHash;
    
    private volatile int currentDifficulty;

    public ConfigService() {
        
    }
    
    @jakarta.annotation.PostConstruct
    public void init() {
        this.currentDifficulty = this.defaultDifficulty;
    }
    
    public int getDifficulty() {
        return currentDifficulty;
    }

    public void setDifficulty(int difficulty) {
        if (difficulty < 1 || difficulty > 10) {
            throw new IllegalArgumentException("La dificultad debe estar entre 1 y 10");
        }
        this.currentDifficulty = difficulty;
    }
    
    public String getInitialSeed() {
        return initialSeed;
    }

    public double getMiningReward() {
        return miningReward;
    }

    public String getGenesisHash() {
        return genesisHash;
    }

    public int getDefaultDifficulty() {
        return defaultDifficulty;
    }

    public void resetToDefaultDifficulty() {
        this.currentDifficulty = this.defaultDifficulty;
    }

    public String getTargetPrefix() {
        return "0".repeat(currentDifficulty);
    }

    public boolean isValidHash(String hash) {
        if (hash == null || hash.length() < currentDifficulty) {
            return false;
        }
        return hash.startsWith(getTargetPrefix());
    }
}