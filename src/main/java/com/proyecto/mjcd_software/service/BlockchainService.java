package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.mjcd_software.model.dto.response.BlockResponse;
import com.proyecto.mjcd_software.model.dto.response.BlockchainResponse;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.repository.BlockRepository;
import com.proyecto.mjcd_software.repository.BlockchainRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlockchainService {
    
    @Autowired
    private BlockchainRepository blockchainRepository;
    
    @Autowired
    private BlockRepository blockRepository;
    
    @Autowired
    private BlockService blockService;
    
    public Blockchain createBlockchain(String name, String description, String creatorIp) {
        Blockchain blockchain = new Blockchain();
        blockchain.setName(name);
        blockchain.setDescription(description);
        blockchain.setCreatorIp(creatorIp);
        
        Blockchain savedBlockchain = blockchainRepository.save(blockchain);
        
        blockService.createGenesisBlock(savedBlockchain.getId());
        
        return savedBlockchain;
    }
    
    public List<BlockResponse> getBlockchainData(String blockchainId) {
        List<Block> blocks = blockRepository.findByBlockchainIdOrderByBlockIndex(blockchainId);
        
        return blocks.stream()
                .map(this::convertToBlockResponse)
                .collect(Collectors.toList());
    }
    
    private BlockResponse convertToBlockResponse(Block block) {
        String data = blockService.getBlockMainContent(block.getId());
        
        return BlockResponse.builder()
                .index(block.getBlockIndex())
                .data(data)
                .previousHash(block.getPreviousHash())
                .hash(block.getCurrentHash())
                .nonce(block.getNonce())
                .timestamp(block.getTimestamp())
                .isValid(block.getIsValid())
                .blockType(block.getBlockType().name().toLowerCase())
                .build();
    }
    
    public Blockchain getDefaultBlockchain() {
        return blockchainRepository.findByIsActiveTrue()
                .stream()
                .findFirst()
                .orElseGet(() -> createBlockchain(
                    "Blockchain Principal", 
                    "Blockchain principal de la aplicación",
                    "127.0.0.1"
                ));
    }

    public Blockchain getBlockchainById(String blockchainId) {
        return blockchainRepository.findById(blockchainId)
                .orElseThrow(() -> new RuntimeException("Blockchain no encontrada con ID: " + blockchainId));
    }

    public List<BlockchainResponse> getActiveBlockchains() {
        List<Blockchain> activeBlockchains = blockchainRepository.findByIsActiveTrue();
        
        return activeBlockchains.stream()
                .map(blockchain -> BlockchainResponse.builder()
                        .id(blockchain.getId())
                        .name(blockchain.getName())
                        .description(blockchain.getDescription())
                        .createdAt(blockchain.getCreatedAt())
                        .updatedAt(blockchain.getUpdatedAt())
                        .isActive(blockchain.getIsActive())
                        .totalBlocks(blockchain.getTotalBlocks())
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, Object> getBlockchainStats(String blockchainId) {
        List<Block> blocks = blockRepository.findByBlockchainIdOrderByBlockIndex(blockchainId);
        
        if (blocks.isEmpty()) {
            return Map.of(
                "totalBlocks", 0,
                "totalTransactions", 0,
                "oldestTime", 0L,
                "newestTime", 0L,
                "averageBlockTime", 0.0,
                "chainIntegrity", 0
            );
        }

        int totalBlocks = blocks.size();
        int totalTransactions = totalBlocks - 1;
        
        Long oldestTime = blocks.stream()
                .mapToLong(Block::getTimestamp)
                .min()
                .orElse(0L);
                
        Long newestTime = blocks.stream()
                .mapToLong(Block::getTimestamp)
                .max()
                .orElse(0L);

        double averageBlockTime = 0.0;
        if (totalBlocks > 1) {
            long totalTime = newestTime - oldestTime;
            averageBlockTime = (double) totalTime / (totalBlocks - 1) / 1000.0;
        }

        long validBlocks = blocks.stream()
                .mapToLong(block -> block.getIsValid() ? 1L : 0L)
                .sum();
        int chainIntegrity = (int) ((validBlocks * 100) / totalBlocks);
        
        return Map.of(
            "totalBlocks", totalBlocks,
            "totalTransactions", totalTransactions,
            "oldestTime", oldestTime,
            "newestTime", newestTime,
            "averageBlockTime", averageBlockTime,
            "chainIntegrity", chainIntegrity,
            "blockchainId", blockchainId,
            "timestamp", System.currentTimeMillis()
        );
    }
}