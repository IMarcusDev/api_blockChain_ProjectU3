package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.request.CreateBlockchainRequest;
import com.proyecto.mjcd_software.model.dto.request.CreateBlockRequest;
import com.proyecto.mjcd_software.model.dto.response.BlockResponse;
import com.proyecto.mjcd_software.model.dto.response.BlockchainResponse;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.BlockService;
import com.proyecto.mjcd_software.util.SecurityUtils;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blockchain")
@CrossOrigin(origins = "*")
public class BlockchainController {
    
    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private BlockService blockService;

    @GetMapping("/chain")
    public ResponseEntity<List<BlockResponse>> getBlockchain() {
        Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
        List<BlockResponse> chain = blockchainService.getBlockchainData(defaultBlockchain.getId());
        return ResponseEntity.ok(chain);
    }

    @GetMapping("/{blockchainId}")
    public ResponseEntity<BlockchainResponse> getBlockchainById(@PathVariable String blockchainId) {
        Blockchain blockchain = blockchainService.getBlockchainById(blockchainId);
        List<BlockResponse> blocks = blockchainService.getBlockchainData(blockchainId);
        
        BlockchainResponse response = BlockchainResponse.builder()
                .id(blockchain.getId())
                .name(blockchain.getName())
                .description(blockchain.getDescription())
                .createdAt(blockchain.getCreatedAt())
                .updatedAt(blockchain.getUpdatedAt())
                .isActive(blockchain.getIsActive())
                .totalBlocks(blockchain.getTotalBlocks())
                .blocks(blocks)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBlockchain(
            @Valid @RequestBody CreateBlockchainRequest request,
            HttpServletRequest httpRequest) {

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        String clientIp = getClientIpAddress(httpRequest);
        Blockchain blockchain = blockchainService.createBlockchain(
            request.getName(), 
            request.getDescription(), 
            clientIp
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Blockchain creada exitosamente",
            "blockchainId", blockchain.getId(),
            "name", blockchain.getName()
        ));
    }

    @PostMapping("/block/text")
    public ResponseEntity<Map<String, Object>> createTextBlock(
            @Valid @RequestBody CreateBlockRequest request) {
        
        // Validar que el usuario esté autenticado
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        String blockchainId = request.getBlockchainId();
        if (blockchainId == null) {
            Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
            blockchainId = defaultBlockchain.getId();
        }
        
        Block newBlock = blockService.createTextBlock(blockchainId, request.getContent(), currentUserId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Bloque creado exitosamente",
            "blockId", newBlock.getId(),
            "blockIndex", newBlock.getBlockIndex(),
            "hash", newBlock.getCurrentHash(),
            "nonce", newBlock.getNonce()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBlockchainStats() {
        Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
        Map<String, Object> stats = blockchainService.getBlockchainStats(defaultBlockchain.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/list")
    public ResponseEntity<List<BlockchainResponse>> listActiveBlockchains() {
        List<BlockchainResponse> blockchains = blockchainService.getActiveBlockchains();
        return ResponseEntity.ok(blockchains);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}