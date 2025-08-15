package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.repository.BlockRepository;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.util.HashGenerator;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserPointsController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BlockRepository blockRepository;

    @GetMapping("/points")
    public ResponseEntity<List<Map<String, Object>>> getUsersWithPoints() {
        try {
            List<User> activeUsers = userRepository.findByIsActiveTrueOrderByTotalPointsDesc();
            
            List<Map<String, Object>> formattedUsers = activeUsers.stream()
                    .map(this::formatUserForFrontend)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(formattedUsers);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    private Map<String, Object> formatUserForFrontend(User user) {
        Map<String, Object> formatted = new HashMap<>();
        
        formatted.put("id", user.getId());
        
        formatted.put("name", user.getFirstName());
        formatted.put("surname", user.getLastName());
        formatted.put("point", user.getTotalPoints() != null ? user.getTotalPoints() : 0);

        formatted.put("chain", getLastMinedBlockHash(user));
        
        formatted.put("avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : 
            generateDefaultAvatar(user.getFirstName(), user.getLastName()));

        formatted.put("status", calculateStatus(user.getTotalPoints()));

        formatted.put("efficiency", calculateEfficiency(user.getTotalPoints()));

        formatted.put("userId", user.getId());

        formatted.put("timestamp", user.getCreatedAt() != null ? 
            user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 
            System.currentTimeMillis());
        
        return formatted;
    }

    private String getLastMinedBlockHash(User user) {
        try {
            List<Block> userBlocks = blockRepository.findByMinedByOrderByBlockIndexDesc(user.getId());
            
            if (!userBlocks.isEmpty()) {
                Block lastBlock = userBlocks.get(0);
                return lastBlock.getCurrentHash();
            }

            List<Block> allBlocks = blockRepository.findTopByOrderByBlockIndexDesc();
            if (!allBlocks.isEmpty()) {
                return allBlocks.get(0).getCurrentHash();
            }
            
            return "0000000000000000000000000000000000000000000000000000000000000000";
            
        } catch (Exception e) {
            String data = user.getId() + user.getEmail() + (user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            return HashGenerator.generateSHA256(data);
        }
    }

    private String generateDefaultAvatar(String firstName, String lastName) {
        return String.format(
            "https://ui-avatars.com/api/?name=%s+%s&background=667eea&color=fff&size=40", 
            firstName, lastName
        );
    }

    private String calculateStatus(Integer points) {
        if (points == null || points == 0) return "low";
        if (points >= 10) return "high";
        if (points >= 5) return "medium";
        return "low";
    }

    private double calculateEfficiency(Integer points) {
        if (points == null || points == 0) return 0.0;
        return Math.min(points * 10.0, 100.0);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            List<User> activeUsers = userRepository.findByIsActiveTrueOrderByTotalPointsDesc();
            
            Long totalUsers = (long) activeUsers.size();
            Long totalPoints = activeUsers.stream()
                    .mapToLong(user -> user.getTotalPoints() != null ? user.getTotalPoints() : 0)
                    .sum();
            
            Double averagePoints = totalUsers > 0 ? (double) totalPoints / totalUsers : 0.0;
            
            Integer maxPoints = activeUsers.stream()
                    .mapToInt(user -> user.getTotalPoints() != null ? user.getTotalPoints() : 0)
                    .max()
                    .orElse(0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", totalUsers);
            response.put("totalPoints", totalPoints);
            response.put("averagePoints", Math.round(averagePoints * 100.0) / 100.0);
            response.put("maxPoints", maxPoints);
            response.put("timestamp", System.currentTimeMillis());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Resto de endpoints si los necesitas...
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllUsers(HttpServletRequest httpRequest) {
        String currentUserId = getCurrentUserId(httpRequest);
        
        // Solo limpiar users_points si existe, pero mantener users
        // userPointsService.clearAllUsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Operación no implementada - mantener usuarios principales");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-random")
    public ResponseEntity<Map<String, Object>> generateRandomUsers(
            @RequestParam(defaultValue = "5") int count,
            HttpServletRequest httpRequest) {
        
        String currentUserId = getCurrentUserId(httpRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Los usuarios ya existen en la tabla principal");
        response.put("count", 0);
        
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        return userId;
    }
}