package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.response.ValidationResponse;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.ValidationService;
import com.proyecto.mjcd_software.util.SecurityUtils;
import com.proyecto.mjcd_software.exception.BlockchainException;

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
        
        return ResponseEntity.ok(Map.of(
            "blockId", blockId,
            "isValid", isValid,
            "timestamp", System.currentTimeMillis()
        ));
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
        
        return ResponseEntity.ok(Map.of(
            "blockchainId", blockchainId,
            "integrityPercentage", integrityPercentage,
            "isValid", validation.getIsValid(),
            "totalBlocks", validation.getDetails().size(),
            "validBlocks", validation.getDetails().stream()
                .mapToLong(detail -> "Valid".equals(detail.getStatus()) ? 1 : 0)
                .sum(),
            "timestamp", System.currentTimeMillis()
        ));
    }

    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateValidation(
            @RequestParam(required = false) String blockchainId) {
        
        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }

        ValidationResponse validation = validationService.validateBlockchain(blockchainId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Validación completada",
            "steps", new String[]{
                "Verificando estructura de bloques...",
                "Validando hashes...",
                "Comprobando enlaces entre bloques...",
                "Verificando timestamps...",
                "Validación completada"
            },
            "result", validation,
            "timestamp", System.currentTimeMillis()
        ));
    }

    @PostMapping("/repair")
    public ResponseEntity<Map<String, Object>> repairBlockchain(
            @RequestParam(required = false) String blockchainId) {
                
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }
        
        int repairedBlocks = validationService.markInvalidBlocks(blockchainId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Proceso de reparación completado",
            "repairedBlocks", repairedBlocks,
            "blockchainId", blockchainId,
            "timestamp", System.currentTimeMillis()
        ));
    }
}