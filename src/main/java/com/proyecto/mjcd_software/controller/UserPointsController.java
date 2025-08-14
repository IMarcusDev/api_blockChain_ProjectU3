package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.service.UserPointsService;
import com.proyecto.mjcd_software.util.SecurityUtils;
import com.proyecto.mjcd_software.exception.BlockchainException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserPointsController {
    
    @Autowired
    private UserPointsService userPointsService;

    @GetMapping("/points")
    public ResponseEntity<List<Map<String, Object>>> getUsersWithPoints() {
        List<Map<String, Object>> users = userPointsService.getAllUsersFormatted();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/points")
    public ResponseEntity<Map<String, Object>> createUserPoints(@RequestBody Map<String, Object> request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        String name = (String) request.get("name");
        String surname = (String) request.get("surname");
        Integer points = (Integer) request.get("points");
        String chainHash = (String) request.get("chainHash");
        
        UserPoints userPoints = userPointsService.createUserPoints(name, surname, points, chainHash, currentUserId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usuario creado exitosamente",
            "userId", userPoints.getId(),
            "name", userPoints.getUserName(),
            "surname", userPoints.getUserSurname(),
            "points", userPoints.getPoints()
        ));
    }

    @PutMapping("/points/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserPoints(
            @PathVariable String userId, 
            @RequestBody Map<String, Object> request) {

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        Integer newPoints = (Integer) request.get("points");
        UserPoints updatedUser = userPointsService.updateUserPoints(userId, newPoints);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Puntos actualizados exitosamente",
            "userId", updatedUser.getId(),
            "points", updatedUser.getPoints(),
            "efficiency", updatedUser.getEfficiency(),
            "status", updatedUser.getStatus().name().toLowerCase()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userPointsService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/generate-random")
    public ResponseEntity<Map<String, Object>> generateRandomUsers(@RequestParam(defaultValue = "5") int count) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        List<UserPoints> generatedUsers = userPointsService.generateRandomUsers(count);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usuarios generados exitosamente",
            "count", generatedUsers.size(),
            "users", generatedUsers
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        UserPoints user = userPointsService.getUserById(userId);
        
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "name", user.getUserName(),
            "surname", user.getUserSurname(),
            "points", user.getPoints(),
            "efficiency", user.getEfficiency(),
            "status", user.getStatus().name().toLowerCase(),
            "chainHash", user.getChainHash(),
            "avatarUrl", user.getAvatarUrl(),
            "createdAt", user.getCreatedAt(),
            "updatedAt", user.getUpdatedAt()
        ));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllUsers() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        userPointsService.clearAllUsers();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Todos los usuarios han sido eliminados"
        ));
    }
}