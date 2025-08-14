package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.request.ConfigUpdateRequest;
import com.proyecto.mjcd_software.service.ConfigService;
import com.proyecto.mjcd_software.util.SecurityUtils;
import com.proyecto.mjcd_software.exception.BlockchainException;

import java.util.Map;

@RestController
@RequestMapping("/config")
@CrossOrigin(origins = "*")
public class ConfigController {
    
    @Autowired
    private ConfigService configService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCurrentConfig() {
        return ResponseEntity.ok(Map.of(
            "difficulty", configService.getDifficulty(),
            "defaultDifficulty", configService.getDefaultDifficulty(),
            "initialSeed", configService.getInitialSeed(),
            "miningReward", configService.getMiningReward(),
            "targetPrefix", configService.getTargetPrefix()
        ));
    }

    @PostMapping("/difficulty")
    public ResponseEntity<Map<String, Object>> updateDifficulty(@RequestBody ConfigUpdateRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        try {
            int newDifficulty = request.getDifficulty();
            configService.setDifficulty(newDifficulty);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dificultad actualizada correctamente",
                "difficulty", newDifficulty,
                "targetPrefix", configService.getTargetPrefix()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetConfig() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        configService.resetToDefaultDifficulty();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Configuración reseteada al valor por defecto",
            "difficulty", configService.getDifficulty()
        ));
    }
}