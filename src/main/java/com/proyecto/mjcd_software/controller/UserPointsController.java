package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.service.UserPointsService;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserPointsController {
    
    @Autowired
    private UserPointsService userPointsService;

    @GetMapping("/points")
    public ResponseEntity<List<Map<String, Object>>> getUsersWithPoints() {
        List<Map<String, Object>> users = userPointsService.getAllUsersFormatted();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/points")
    public ResponseEntity<Map<String, Object>> createUserPoints(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        String currentUserId = getCurrentUserId(httpRequest);
        
        String name = (String) request.get("name");
        String surname = (String) request.get("surname");
        Integer points = (Integer) request.get("points");
        String chainHash = (String) request.get("chainHash");
        
        UserPoints userPoints = userPointsService.createUserPoints(name, surname, points, chainHash, currentUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Usuario creado exitosamente");
        response.put("userId", userPoints.getId());
        response.put("name", userPoints.getUserName());
        response.put("surname", userPoints.getUserSurname());
        response.put("points", userPoints.getPoints());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/points/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserPoints(
            @PathVariable String userId, 
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        String currentUserId = getCurrentUserId(httpRequest);
        
        Integer newPoints = (Integer) request.get("points");
        UserPoints updatedUser = userPointsService.updateUserPoints(userId, newPoints);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Puntos actualizados exitosamente");
        response.put("userId", updatedUser.getId());
        response.put("points", updatedUser.getPoints());
        response.put("efficiency", updatedUser.getEfficiency());
        response.put("status", updatedUser.getStatus().name().toLowerCase());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userPointsService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/generate-random")
    public ResponseEntity<Map<String, Object>> generateRandomUsers(
            @RequestParam(defaultValue = "5") int count,
            HttpServletRequest httpRequest) {
        
        String currentUserId = getCurrentUserId(httpRequest);
        
        List<UserPoints> generatedUsers = userPointsService.generateRandomUsers(count);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Usuarios generados exitosamente");
        response.put("count", generatedUsers.size());
        response.put("users", generatedUsers);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        UserPoints user = userPointsService.getUserById(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getUserName());
        response.put("surname", user.getUserSurname());
        response.put("points", user.getPoints());
        response.put("efficiency", user.getEfficiency());
        response.put("status", user.getStatus().name().toLowerCase());
        response.put("chainHash", user.getChainHash());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("createdAt", user.getCreatedAt());
        response.put("updatedAt", user.getUpdatedAt());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllUsers(HttpServletRequest httpRequest) {
        String currentUserId = getCurrentUserId(httpRequest);
        
        userPointsService.clearAllUsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Todos los usuarios han sido eliminados");
        
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