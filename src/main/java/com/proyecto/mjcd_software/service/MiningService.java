package com.proyecto.mjcd_software.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.util.HashGenerator;

@Slf4j
@Service
public class MiningService {
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private UserRepository userRepository;

    public Block mineBlock(Block block, String data, String userId) {
        if (block == null) {
            throw new IllegalArgumentException("El bloque no puede ser nulo");
        }
        if (data == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos");
        }
        
        int difficulty = configService.getDifficulty();
        String target = "0".repeat(difficulty);
        
        log.info("Iniciando minado de bloque {} para blockchain {} con dificultad {}", 
                block.getBlockIndex(), block.getBlockchainId(), difficulty);
        
        long startTime = System.currentTimeMillis();
        long attempts = 0;
        
        do {
            block.setNonce(block.getNonce() + 1);
            attempts++;
            String blockData = generateBlockData(block, data);
            block.setCurrentHash(HashGenerator.generateSHA256(blockData));
            if (attempts % 100000 == 0) {
                log.debug("Intento {}: hash actual = {}", attempts, block.getCurrentHash());
            }
        } while (!block.getCurrentHash().startsWith(target));
        
        long endTime = System.currentTimeMillis();
        long miningTime = endTime - startTime;
        
        log.info("Bloque minado exitosamente en {}ms con {} intentos", miningTime, attempts);
        log.info("Hash encontrado: {}", block.getCurrentHash());
        log.info("Nonce: {}", block.getNonce());
        if (userId != null) {
            rewardUser(userId, block);
        }
        
        return block;
    }

    public Block mineBlock(Block block, String data) {
        return mineBlock(block, data, null);
    }
    
    @Transactional
    private void rewardUser(String userId, Block block) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getIsActive()) {
                block.setMinedBy(userId);
                block.setMiningReward(configService.getMiningReward());
                user.setTotalPoints(user.getTotalPoints() + 1);
                user.setBlocksMined(user.getBlocksMined() + 1);
                userRepository.save(user);
                
                log.info("Recompensa otorgada a usuario: {} {} (ID: {})", 
                        user.getFirstName(), user.getLastName(), userId);
            } else {
                log.warn("Usuario {} no encontrado o inactivo para otorgar recompensa", userId);
            }
        } catch (Exception e) {
            log.error("Error al otorgar recompensa al usuario {}: {}", userId, e.getMessage());
        }
    }
    
    private String generateBlockData(Block block, String content) {
        return String.format("{id:%d,hashPrevio:%s,timeStamp:%d,contenido:%s,nonce:%d}",
                block.getBlockIndex(),
                block.getPreviousHash() != null ? block.getPreviousHash() : "null",
                block.getTimestamp(),
                content,
                block.getNonce());
    }
}