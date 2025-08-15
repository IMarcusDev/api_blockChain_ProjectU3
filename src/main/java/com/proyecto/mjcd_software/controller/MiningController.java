package com.proyecto.mjcd_software.controller;

import java.util.HashMap;
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
        
        String currentUserId = getCurrentUserId(httpRequest);
        
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bloque minado exitosamente");
            
            Map<String, Object> miningInfo = new HashMap<>();
            miningInfo.put("blockId", newBlock.getId());
            miningInfo.put("blockIndex", newBlock.getBlockIndex());
            miningInfo.put("hash", newBlock.getCurrentHash());
            miningInfo.put("nonce", newBlock.getNonce());
            miningInfo.put("difficulty", newBlock.getDifficulty());
            miningInfo.put("pointsEarned", newBlock.getMiningReward() != null ? newBlock.getMiningReward().intValue() : 1);
            response.put("mining", miningInfo);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("totalPoints", updatedUser != null ? updatedUser.getTotalPoints() : 0);
            userInfo.put("blocksMined", updatedUser != null ? updatedUser.getBlocksMined() : 0);
            response.put("user", userInfo);
            
            Map<String, Object> userPointsInfo = new HashMap<>();
            userPointsInfo.put("points", userPoints != null ? userPoints.getPoints() : 0);
            userPointsInfo.put("status", userPoints != null ? userPoints.getStatus().name().toLowerCase() : "low");
            userPointsInfo.put("efficiency", userPoints != null ? userPoints.getEfficiency().doubleValue() : 0.0);
            response.put("userPoints", userPointsInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMiningStats(HttpServletRequest httpRequest) {
        String currentUserId = getCurrentUserId(httpRequest);
        
        try {
            User user = userRepository.findById(currentUserId).orElse(null);
            UserPoints userPoints = userPointsService.findOrCreateUserPoints(currentUserId);
            int currentDifficulty = configService.getDifficulty();
            
            int totalPoints = user != null ? user.getTotalPoints() : 0;
            int blocksMined = user != null ? user.getBlocksMined() : 0;

            double averagePointsPerBlock = blocksMined > 0 ? (double) totalPoints / blocksMined : 0.0;
            
            Map<String, Object> userStatsInfo = new HashMap<>();
            userStatsInfo.put("totalPoints", totalPoints);
            userStatsInfo.put("blocksMined", blocksMined);
            userStatsInfo.put("averagePointsPerBlock", Math.round(averagePointsPerBlock * 100.0) / 100.0);
            userStatsInfo.put("userPointsTotal", userPoints != null ? userPoints.getPoints() : 0);
            userStatsInfo.put("efficiency", userPoints != null ? userPoints.getEfficiency().doubleValue() : 0.0);
            userStatsInfo.put("status", userPoints != null ? userPoints.getStatus().name().toLowerCase() : "low");
            
            Map<String, Object> networkStatsInfo = new HashMap<>();
            networkStatsInfo.put("currentDifficulty", currentDifficulty);
            networkStatsInfo.put("estimatedPointsReward", 1);
            networkStatsInfo.put("targetPrefix", "0".repeat(currentDifficulty));
            
            Map<String, Object> response = new HashMap<>();
            response.put("userStats", userStatsInfo);
            response.put("networkStats", networkStatsInfo);
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de minado para usuario {}: {}", currentUserId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard() {
        try {
            Map<String, Object> userStats = userPointsService.getUserStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("leaderboard", userStats);
            response.put("message", "Clasificación obtenida exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String getCurrentUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        return userId;
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