package com.proyecto.mjcd_software.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.exception.BlockchainException;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.service.BlockService;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.ConfigService;
import com.proyecto.mjcd_software.service.UserPointsService;
import com.proyecto.mjcd_software.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mining")
public class MiningController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MiningController.class);

    @Autowired
    private BlockService blockService;
    
    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private UserPointsService userPointsService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startMining(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                content = "Bloque minado por " + getCurrentUserName(currentUserId);
            }
            
            String blockchainId = request.get("blockchainId");
            if (blockchainId == null) {
                Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
                blockchainId = defaultBlockchain.getId();
            }

            Block newBlock = blockService.createTextBlock(blockchainId, content, currentUserId);

            User updatedUser = userRepository.findById(currentUserId).orElse(null);
            UserPoints userPoints = userPointsService.findOrCreateUserPoints(currentUserId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bloque minado exitosamente",
                "mining", Map.of(
                    "blockId", newBlock.getId(),
                    "blockIndex", newBlock.getBlockIndex(),
                    "hash", newBlock.getCurrentHash(),
                    "nonce", newBlock.getNonce(),
                    "difficulty", newBlock.getDifficulty(),
                    "pointsEarned", newBlock.getMiningReward() != null ? newBlock.getMiningReward().intValue() : 1
                ),
                "user", Map.of(
                    "totalPoints", updatedUser != null ? updatedUser.getTotalPoints() : 0,
                    "blocksMined", updatedUser != null ? updatedUser.getBlocksMined() : 0
                ),
                "userPoints", Map.of(
                    "points", userPoints != null ? userPoints.getPoints() : 0,
                    "status", userPoints != null ? userPoints.getStatus().name().toLowerCase() : "low",
                    "efficiency", userPoints != null ? userPoints.getEfficiency().doubleValue() : 0.0
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMiningStats() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        try {
            User user = userRepository.findById(currentUserId).orElse(null);
            UserPoints userPoints = userPointsService.findOrCreateUserPoints(currentUserId);
            int currentDifficulty = configService.getDifficulty();
            
            int totalPoints = user != null ? user.getTotalPoints() : 0;
            int blocksMined = user != null ? user.getBlocksMined() : 0;

            double averagePointsPerBlock = blocksMined > 0 ? (double) totalPoints / blocksMined : 0.0;
            
            Map<String, Object> stats = Map.of(
                "userStats", Map.of(
                    "totalPoints", totalPoints,
                    "blocksMined", blocksMined,
                    "averagePointsPerBlock", Math.round(averagePointsPerBlock * 100.0) / 100.0,
                    "userPointsTotal", userPoints != null ? userPoints.getPoints() : 0,
                    "efficiency", userPoints != null ? userPoints.getEfficiency().doubleValue() : 0.0,
                    "status", userPoints != null ? userPoints.getStatus().name().toLowerCase() : "low"
                ),
                "networkStats", Map.of(
                    "currentDifficulty", currentDifficulty,
                    "estimatedPointsReward", 1,
                    "targetPrefix", "0".repeat(currentDifficulty)
                ),
                "success", true,
                "timestamp", System.currentTimeMillis()
            );
            
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de minado para usuario {}: {}", currentUserId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        try {
            Map<String, Object> userStats = userPointsService.getUserStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "leaderboard", userStats,
                "message", "Clasificación obtenida exitosamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    private String getCurrentUserName(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            return user != null ? user.getFirstName() + " " + user.getLastName() : "Usuario";
        } catch (Exception e) {
            return "Usuario";
        }
    }
}