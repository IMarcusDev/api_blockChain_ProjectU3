package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.response.ValidationResponse;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.ValidationService;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/validation")
public class ValidationController {
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private BlockchainService blockchainService;

    @GetMapping("/blockchain")
    public ResponseEntity<ValidationResponse> validateBlockchain(
            @RequestParam(required = false) String blockchainId) {

        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }
        
        ValidationResponse validation = validationService.validateBlockchain(blockchainId);
        return ResponseEntity.ok(validation);
    }

    @GetMapping("/block/{blockId}")
    public ResponseEntity<Map<String, Object>> validateBlock(@PathVariable String blockId) {
        boolean isValid = validationService.validateSingleBlock(blockId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("blockId", blockId);
        response.put("isValid", isValid);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/integrity")
    public ResponseEntity<Map<String, Object>> getChainIntegrity(
            @RequestParam(required = false) String blockchainId) {
        
        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }
        
        int integrityPercentage = validationService.calculateChainIntegrity(blockchainId);
        ValidationResponse validation = validationService.validateBlockchain(blockchainId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("blockchainId", blockchainId);
        response.put("integrityPercentage", integrityPercentage);
        response.put("isValid", validation.getIsValid());
        response.put("totalBlocks", validation.getDetails().size());
        response.put("validBlocks", validation.getDetails().stream()
            .mapToLong(detail -> "Valid".equals(detail.getStatus()) ? 1 : 0)
            .sum());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateValidation(
            @RequestParam(required = false) String blockchainId) {
        
        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }

        ValidationResponse validation = validationService.validateBlockchain(blockchainId);
        
        String[] steps = {
            "Verificando estructura de bloques...",
            "Validando hashes...",
            "Comprobando enlaces entre bloques...",
            "Verificando timestamps...",
            "Validación completada"
        };
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Validación completada");
        response.put("steps", steps);
        response.put("result", validation);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/repair")
    public ResponseEntity<Map<String, Object>> repairBlockchain(
            @RequestParam(required = false) String blockchainId,
            HttpServletRequest httpRequest) {
                
        String currentUserId = getCurrentUserId(httpRequest);
        
        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }
        
        int repairedBlocks = validationService.markInvalidBlocks(blockchainId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Proceso de reparación completado");
        response.put("repairedBlocks", repairedBlocks);
        response.put("blockchainId", blockchainId);
        response.put("timestamp", System.currentTimeMillis());
        
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