package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.mjcd_software.model.dto.response.ValidationResponse;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.repository.BlockRepository;

import java.util.List;
import java.util.ArrayList;

@Service
public class ValidationService {
    
    @Autowired
    private BlockRepository blockRepository;
    
    public ValidationResponse validateBlockchain(String blockchainId) {
        List<Block> blocks = blockRepository.findByBlockchainIdOrderByBlockIndex(blockchainId);
        
        if (blocks.isEmpty()) {
            return ValidationResponse.builder()
                    .isValid(false)
                    .message("No se encontraron bloques en la blockchain")
                    .details(new ArrayList<>())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        
        List<ValidationResponse.ValidationDetail> details = new ArrayList<>();
        boolean isChainValid = true;
        
        for (int i = 0; i < blocks.size(); i++) {
            Block currentBlock = blocks.get(i);
            ValidationResponse.ValidationDetail detail = validateBlock(currentBlock, i == 0 ? null : blocks.get(i - 1));
            details.add(detail);
            
            if (!detail.getStatus().equals("Valid")) {
                isChainValid = false;
            }
        }
        
        String message = isChainValid ? 
            "Cadena validada correctamente - Integridad verificada" : 
            "Se encontraron inconsistencias en la cadena";
            
        return ValidationResponse.builder()
                .isValid(isChainValid)
                .message(message)
                .details(details)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private ValidationResponse.ValidationDetail validateBlock(Block currentBlock, Block previousBlock) {
        String status;
        String message;
        String expectedPreviousHash = null;
        
        if (currentBlock.getBlockIndex() == 0) {
            if (currentBlock.getPreviousHash() == null) {
                status = "Valid";
                message = "Bloque génesis válido";
            } else {
                status = "Invalid";
                message = "Bloque génesis debe tener previousHash null";
            }
        } else {
            if (previousBlock != null) {
                expectedPreviousHash = previousBlock.getCurrentHash();
                
                if (currentBlock.getPreviousHash().equals(expectedPreviousHash)) {
                    status = "Valid";
                    message = "Enlaces de hash correctos";
                } else {
                    status = "Invalid";
                    message = "Hash anterior no coincide";
                }
            } else {
                status = "Invalid";
                message = "Bloque anterior no encontrado";
            }
        }
        
        return ValidationResponse.ValidationDetail.builder()
                .blockIndex(currentBlock.getBlockIndex())
                .currentHash(currentBlock.getCurrentHash())
                .previousHash(currentBlock.getPreviousHash())
                .expectedPreviousHash(expectedPreviousHash)
                .status(status)
                .message(message)
                .build();
    }
    
    public boolean isBlockValid(Block block) {
        String target = new String(new char[block.getDifficulty()]).replace('\0', '0');
        return block.getCurrentHash().startsWith(target);
    }
    
    public int calculateChainIntegrity(String blockchainId) {
        ValidationResponse validation = validateBlockchain(blockchainId);
        
        if (validation.getDetails().isEmpty()) {
            return 0;
        }
        
        long validBlocks = validation.getDetails().stream()
                .mapToLong(detail -> "Valid".equals(detail.getStatus()) ? 1 : 0)
                .sum();
                
        return (int) ((validBlocks * 100) / validation.getDetails().size());
    }

    public boolean validateSingleBlock(String blockId) {
        try {
            Block block = blockRepository.findById(blockId)
                    .orElseThrow(() -> new RuntimeException("Bloque no encontrado con ID: " + blockId));
            
            if (block.getCurrentHash() == null || block.getTimestamp() == null) {
                return false;
            }

            if (!isBlockValid(block)) {
                return false;
            }
            
            if (block.getBlockIndex() > 0) {
                Block previousBlock = blockRepository.findByBlockchainIdAndBlockIndex(
                    block.getBlockchainId(), 
                    block.getBlockIndex() - 1
                ).orElse(null);
                
                if (previousBlock == null) {
                    return false;
                }

                if (!block.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error validando bloque " + blockId + ": " + e.getMessage());
            return false;
        }
    }

    public int markInvalidBlocks(String blockchainId) {
        List<Block> blocks = blockRepository.findByBlockchainIdOrderByBlockIndex(blockchainId);
        int repairedCount = 0;
        
        for (Block block : blocks) {
            boolean shouldBeValid = validateSingleBlock(block.getId());
            
            if (block.getIsValid() != shouldBeValid) {
                block.setIsValid(shouldBeValid);
                blockRepository.save(block);
                repairedCount++;
                
                System.out.println("Bloque " + block.getBlockIndex() + 
                    " marcado como " + (shouldBeValid ? "válido" : "inválido"));
            }
        }
        
        return repairedCount;
    }
}