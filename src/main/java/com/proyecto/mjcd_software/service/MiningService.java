package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.repository.UserPointsRepository;
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
    
    @Autowired
    private UserPointsRepository userPointsRepository;

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

        // Cada bloque minado = 1 punto (sistema simplificado)
        int pointsEarned = 1;
        
        log.info("Bloque minado exitosamente!");
        log.info("Tiempo: {}ms | Intentos: {} | Hash: {}", 
                miningTime, attempts, block.getCurrentHash());
        log.info("Puntos ganados: {}", pointsEarned);
        
        if (userId != null) {
            rewardUser(userId, block, pointsEarned, attempts, miningTime);
        }
        
        return block;
    }
    
    @Transactional
    private void rewardUser(String userId, Block block, int points, long attempts, long miningTime) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getIsActive()) {
                // Actualizar la tabla users
                block.setMinedBy(userId);
                block.setMiningReward((double) points);
                
                user.setTotalPoints(user.getTotalPoints() + points);
                user.setBlocksMined(user.getBlocksMined() + 1);
                userRepository.save(user);
                
                // Buscar o crear registro en users_points
                UserPoints userPoints = userPointsRepository
                    .findByUser_Id(userId)
                    .orElse(null);
                
                if (userPoints == null) {
                    // Crear nuevo registro en users_points
                    userPoints = new UserPoints();
                    userPoints.setUser(user);
                    userPoints.setUserName(user.getFirstName());
                    userPoints.setUserSurname(user.getLastName());
                    userPoints.setPoints(points);
                    userPoints.setAvatarUrl(user.getAvatarUrl());
                    userPoints.setBlockchainId(block.getBlockchainId());
                    userPoints.setChainHash(generateUserChainHash(user, block));
                } else {
                    // Actualizar registro existente
                    userPoints.setPoints(userPoints.getPoints() + points);
                    userPoints.setChainHash(generateUserChainHash(user, block));
                }
                
                // Calcular eficiencia y estado basado en puntos totales
                calculateEfficiencyAndStatus(userPoints);
                userPointsRepository.save(userPoints);
                
                log.info("Recompensa otorgada a {} {} (ID: {})", 
                        user.getFirstName(), user.getLastName(), userId);
                log.info("Nuevos totales - Puntos: {} | Bloques minados: {}", 
                        user.getTotalPoints(), user.getBlocksMined());
                log.info("UserPoints actualizado - Puntos: {} | Estado: {}", 
                        userPoints.getPoints(), userPoints.getStatus());
                
            } else {
                log.warn("Usuario {} no encontrado o inactivo para otorgar recompensa", userId);
            }
        } catch (Exception e) {
            log.error("Error al otorgar recompensa al usuario {}: {}", userId, e.getMessage());
        }
    }
    
    private void calculateEfficiencyAndStatus(UserPoints userPoints) {
        Integer points = userPoints.getPoints();
        double efficiency = Math.min(points * 10.0, 100.0);
        userPoints.setEfficiency(java.math.BigDecimal.valueOf(efficiency));

        if (points >= 10) {
            userPoints.setStatus(UserPoints.Status.HIGH);
        } else if (points >= 5) {
            userPoints.setStatus(UserPoints.Status.MEDIUM);
        } else {
            userPoints.setStatus(UserPoints.Status.LOW);
        }
    }
    
    private String generateUserChainHash(User user, Block block) {
        String data = user.getId() + user.getEmail() + block.getCurrentHash() + System.currentTimeMillis();
        return HashGenerator.generateSHA256(data);
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