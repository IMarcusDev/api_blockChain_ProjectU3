package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.response.BlockResponse;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.service.BlockService;

import java.util.Map;

@RestController
@RequestMapping("/block")
public class BlockController {
    
    @Autowired
    private BlockService blockService;

    @GetMapping("/{blockId}")
    public ResponseEntity<BlockResponse> getBlockById(@PathVariable String blockId) {
        Block block = blockService.getBlockById(blockId);
        String data = blockService.getBlockMainContent(blockId);
        
        BlockResponse response = BlockResponse.builder()
                .index(block.getBlockIndex())
                .data(data)
                .previousHash(block.getPreviousHash())
                .hash(block.getCurrentHash())
                .nonce(block.getNonce())
                .timestamp(block.getTimestamp())
                .isValid(block.getIsValid())
                .blockType(block.getBlockType().name().toLowerCase())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{blockId}/content")
    public ResponseEntity<Map<String, Object>> getBlockContent(@PathVariable String blockId) {
        String content = blockService.getBlockMainContent(blockId);
        Block block = blockService.getBlockById(blockId);
        
        return ResponseEntity.ok(Map.of(
            "blockId", blockId,
            "content", content,
            "blockType", block.getBlockType().name().toLowerCase(),
            "timestamp", block.getTimestamp()
        ));
    }

    @GetMapping("/{blockId}/validate")
    public ResponseEntity<Map<String, Object>> validateBlock(@PathVariable String blockId) {
        Block block = blockService.getBlockById(blockId);
        boolean isValid = blockService.validateBlock(block);
        
        return ResponseEntity.ok(Map.of(
            "blockId", blockId,
            "isValid", isValid,
            "hash", block.getCurrentHash(),
            "previousHash", block.getPreviousHash()
        ));
    }

    @GetMapping("/blockchain/{blockchainId}/last-hash")
    public ResponseEntity<Map<String, Object>> getLastBlockHash(@PathVariable String blockchainId) {
        String lastHash = blockService.getLastBlockHash(blockchainId);
        Integer nextIndex = blockService.getNextBlockIndex(blockchainId);
        
        return ResponseEntity.ok(Map.of(
            "lastHash", lastHash,
            "nextIndex", nextIndex,
            "blockchainId", blockchainId
        ));
    }
}