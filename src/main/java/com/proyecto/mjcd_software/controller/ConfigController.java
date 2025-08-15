package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.request.ConfigUpdateRequest;
import com.proyecto.mjcd_software.service.ConfigService;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigController {
    
    @Autowired
    private ConfigService configService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCurrentConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("difficulty", configService.getDifficulty());
        response.put("defaultDifficulty", configService.getDefaultDifficulty());
        response.put("initialSeed", configService.getInitialSeed());
        response.put("miningReward", configService.getMiningReward());
        response.put("targetPrefix", configService.getTargetPrefix());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/difficulty")
    public ResponseEntity<Map<String, Object>> updateDifficulty(
            @RequestBody ConfigUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        String currentUserId = getCurrentUserId(httpRequest);
        
        try {
            int newDifficulty = request.getDifficulty();
            configService.setDifficulty(newDifficulty);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dificultad actualizada correctamente");
            response.put("difficulty", newDifficulty);
            response.put("targetPrefix", configService.getTargetPrefix());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetConfig(HttpServletRequest httpRequest) {
        String currentUserId = getCurrentUserId(httpRequest);
        
        configService.resetToDefaultDifficulty();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Configuración reseteada al valor por defecto");
        response.put("difficulty", configService.getDifficulty());
        
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