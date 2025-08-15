package com.proyecto.mjcd_software.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.exception.BlockchainException;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.service.BlockService;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.ConfigService;
import com.proyecto.mjcd_software.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/mining")
public class MiningController {
    
    @Autowired
    private BlockService blockService;
    
    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConfigService configService;

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
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bloque minado exitosamente",
                "mining", Map.of(
                    "blockId", newBlock.getId(),
                    "blockIndex", newBlock.getBlockIndex(),
                    "hash", newBlock.getCurrentHash(),
                    "nonce", newBlock.getNonce(),
                    "difficulty", newBlock.getDifficulty(),
                    "pointsEarned", newBlock.getMiningReward() != null ? newBlock.getMiningReward().intValue() : 0
                ),
                "user", Map.of(
                    "totalPoints", updatedUser != null ? updatedUser.getTotalPoints() : 0,
                    "blocksMined", updatedUser != null ? updatedUser.getBlocksMined() : 0
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
            int currentDifficulty = configService.getDifficulty();
            Map<String, Object> stats = Map.of(
                "userStats", Map.of(
                    "totalPoints", user != null ? user.getTotalPoints() : 0,
                    "blocksMined", user != null ? user.getBlocksMined() : 0,
                    "averagePointsPerBlock", user != null && user.getBlocksMined() > 0 ? 
                        user.getTotalPoints() / user.getBlocksMined() : 0
                ),
                "networkStats", Map.of(
                    "currentDifficulty", currentDifficulty,
                    "estimatedPointsReward", currentDifficulty * 10 + 10,
                    "targetPrefix", "0".repeat(currentDifficulty)
                )
            );
            
            return ResponseEntity.ok(stats);
            
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