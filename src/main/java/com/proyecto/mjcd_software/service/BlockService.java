package com.proyecto.mjcd_software.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.BlockData;
import com.proyecto.mjcd_software.repository.BlockDataRepository;
import com.proyecto.mjcd_software.repository.BlockRepository;
import com.proyecto.mjcd_software.util.Constants;
import com.proyecto.mjcd_software.util.HashGenerator;
import com.proyecto.mjcd_software.exception.BlockchainException;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BlockService {
    
    @Autowired
    private BlockRepository blockRepository;
    
    @Autowired
    private BlockDataRepository blockDataRepository;
    
    @Autowired
    private MiningService miningService;
    
    @Transactional
    public Block createGenesisBlock(String blockchainId) {
        log.info("Creando bloque génesis para blockchain: {}", blockchainId);
        
        Block genesisBlock = new Block();
        genesisBlock.setBlockchainId(blockchainId);
        genesisBlock.setBlockIndex(0);

        genesisBlock.setPreviousHash(Constants.GENESIS_PREV_HASH);
        genesisBlock.setBlockType(Block.BlockType.GENESIS);
        genesisBlock.setTimestamp(System.currentTimeMillis());

        Block minedBlock = miningService.mineBlock(genesisBlock, Constants.GENESIS_DATA, null);
        Block savedBlock = blockRepository.save(minedBlock);
        
        BlockData genesisData = new BlockData();
        genesisData.setBlockId(savedBlock.getId());
        genesisData.setDataType(BlockData.DataType.TEXT);
        genesisData.setContent(Constants.GENESIS_DATA);
        genesisData.setContentHash(HashGenerator.generateSHA256(Constants.GENESIS_DATA));
        blockDataRepository.save(genesisData);
        
        log.info("Bloque génesis creado exitosamente con ID: {}", savedBlock.getId());
        return savedBlock;
    }

    @Transactional
    public Block createTextBlock(String blockchainId, String content, String userId) {
        log.info("Creando bloque de texto para blockchain: {} por usuario: {}", blockchainId, userId);
        
        if (content == null || content.trim().isEmpty()) {
            throw new BlockchainException("El contenido del bloque no puede estar vacío");
        }
        
        String previousHash = getLastBlockHash(blockchainId);
        Integer nextIndex = getNextBlockIndex(blockchainId);
        
        Block newBlock = new Block();
        newBlock.setBlockchainId(blockchainId);
        newBlock.setBlockIndex(nextIndex);
        newBlock.setPreviousHash(previousHash);
        newBlock.setBlockType(Block.BlockType.TEXT);
        newBlock.setTimestamp(System.currentTimeMillis());

        Block minedBlock = miningService.mineBlock(newBlock, content, userId);
        Block savedBlock = blockRepository.save(minedBlock);

        BlockData blockData = new BlockData();
        blockData.setBlockId(savedBlock.getId());
        blockData.setDataType(BlockData.DataType.TEXT);
        blockData.setContent(content);
        blockData.setContentHash(HashGenerator.generateSHA256(content));
        blockDataRepository.save(blockData);

        if (userId != null) {
            log.info("Bloque de texto creado exitosamente con puntos de minado incluidos");
        }
        
        log.info("Bloque de texto creado exitosamente: índice {}, hash: {}", 
                savedBlock.getBlockIndex(), savedBlock.getCurrentHash());
        return savedBlock;
    }

    @Transactional
    public Block createTextBlock(String blockchainId, String content) {
        return createTextBlock(blockchainId, content, null);
    }
    
    public String getBlockMainContent(String blockId) {
        if (blockId == null) {
            throw new BlockchainException("ID de bloque no puede ser nulo");
        }
        
        return blockDataRepository.findByBlockIdAndDataType(blockId, BlockData.DataType.TEXT)
                .map(BlockData::getContent)
                .orElse("Sin contenido");
    }
    
    public String getLastBlockHash(String blockchainId) {
        if (blockchainId == null) {
            throw new BlockchainException("ID de blockchain no puede ser nulo");
        }
        
        return blockRepository.findTopByBlockchainIdOrderByBlockIndexDesc(blockchainId)
                .map(Block::getCurrentHash)
                .orElse(Constants.GENESIS_PREV_HASH);
    }
    
    public Integer getNextBlockIndex(String blockchainId) {
        if (blockchainId == null) {
            throw new BlockchainException("ID de blockchain no puede ser nulo");
        }
        
        return blockRepository.findTopByBlockchainIdOrderByBlockIndexDesc(blockchainId)
                .map(block -> block.getBlockIndex() + 1)
                .orElse(0);
    }

    @Transactional
    public Block mineAndSaveBlock(Block block, String content, String userId) {
        Block minedBlock = miningService.mineBlock(block, content, userId);
        return blockRepository.save(minedBlock);
    }

    @Transactional
    public Block mineAndSaveBlock(Block block, String content) {
        return mineAndSaveBlock(block, content, null);
    }

    public Block getBlockById(String blockId) {
        if (blockId == null) {
            throw new BlockchainException("ID de bloque no puede ser nulo");
        }
        
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new BlockchainException("Bloque no encontrado con ID: " + blockId));
    }

    public boolean validateBlock(Block block) {
        if (block == null) {
            log.warn("Intento de validar bloque nulo");
            return false;
        }
        
        String currentHash = block.getCurrentHash();
        if (currentHash == null) {
            log.warn("Bloque {} no tiene hash", block.getId());
            return false;
        }

        int difficulty = block.getDifficulty();
        String target = "0".repeat(difficulty);
        
        if (!currentHash.startsWith(target)) {
            log.warn("Hash del bloque {} no cumple con la dificultad requerida", block.getId());
            return false;
        }

        String content = getBlockMainContent(block.getId());
        String regeneratedHash = generateBlockHash(block, content);
        
        boolean isValid = currentHash.equals(regeneratedHash);
        if (!isValid) {
            log.warn("Hash regenerado no coincide para bloque {}", block.getId());
        }
        
        return isValid;
    }

    private String generateBlockHash(Block block, String content) {
        String blockData = String.format("{id:%d,hashPrevio:%s,timeStamp:%d,contenido:%s,nonce:%d}",
                block.getBlockIndex(),
                block.getPreviousHash() != null ? block.getPreviousHash() : "null",
                block.getTimestamp(),
                content,
                block.getNonce());
        
        return HashGenerator.generateSHA256(blockData);
    }
}