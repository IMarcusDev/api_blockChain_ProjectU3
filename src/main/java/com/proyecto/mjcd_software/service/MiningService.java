package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.util.HashGenerator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

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
            
            if (attempts % 50000 == 0) {
                log.debug("⚡ Intento {}: hash actual = {}", attempts, block.getCurrentHash());
            }
        } while (!block.getCurrentHash().startsWith(target));
        
        long endTime = System.currentTimeMillis();
        long miningTime = endTime - startTime;

        int pointsEarned = calculateMiningReward(difficulty, attempts, miningTime);
        
        log.info("Bloque minado exitosamente!");
        log.info("Tiempo: {}ms | Intentos: {} | Hash: {}", 
                miningTime, attempts, block.getCurrentHash());
        log.info("Puntos ganados: {}", pointsEarned);
        
        if (userId != null) {
            rewardUser(userId, block, pointsEarned, attempts, miningTime);
        }
        
        return block;
    }

    private int calculateMiningReward(int difficulty, long attempts, long miningTime) {
        int basePoints = difficulty * 10;
        int attemptsBonus = (int) Math.min(attempts / 1000, 50);
        int timeBonus = miningTime < 5000 ? 20 : miningTime < 10000 ? 10 : 5;
        
        return basePoints + attemptsBonus + timeBonus;
    }
    
    @Transactional
    private void rewardUser(String userId, Block block, int points, long attempts, long miningTime) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getIsActive()) {
                block.setMinedBy(userId);
                block.setMiningReward((double) points);
                
                user.setTotalPoints(user.getTotalPoints() + points);
                user.setBlocksMined(user.getBlocksMined() + 1);
                userRepository.save(user);
                
                log.info("Recompensa otorgada a {} {} (ID: {})", 
                        user.getFirstName(), user.getLastName(), userId);
                log.info("Nuevos totales - Puntos: {} | Bloques minados: {}", 
                        user.getTotalPoints(), user.getBlocksMined());

                createMiningNotification(user, points, attempts, miningTime);
                
            } else {
                log.warn("Usuario {} no encontrado o inactivo para otorgar recompensa", userId);
            }
        } catch (Exception e) {
            log.error("Error al otorgar recompensa al usuario {}: {}", userId, e.getMessage());
        }
    }
    
    private void createMiningNotification(User user, int points, long attempts, long miningTime) {
        log.info("Notificación: {} ha minado un bloque y ganado {} puntos!", 
                user.getFirstName(), points);
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