package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.BlockData;
import com.proyecto.mjcd_software.repository.BlockDataRepository;
import com.proyecto.mjcd_software.repository.BlockRepository;
import com.proyecto.mjcd_software.util.Constants;
import com.proyecto.mjcd_software.util.HashGenerator;

@Service
@Transactional
public class BlockService {
    
    @Autowired
    private BlockRepository blockRepository;
    
    @Autowired
    private BlockDataRepository blockDataRepository;
    
    @Autowired
    private MiningService miningService;
    
    public Block createGenesisBlock(String blockchainId) {
        Block genesisBlock = new Block();
        genesisBlock.setBlockchainId(blockchainId);
        genesisBlock.setBlockIndex(0);
        genesisBlock.setPreviousHash(null);
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
        
        return savedBlock;
    }

    public Block createTextBlock(String blockchainId, String content, String userId) {
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
        
        return savedBlock;
    }

    public Block createTextBlock(String blockchainId, String content) {
        return createTextBlock(blockchainId, content, null);
    }
    
    public String getBlockMainContent(String blockId) {
        return blockDataRepository.findByBlockIdAndDataType(blockId, BlockData.DataType.TEXT)
                .map(BlockData::getContent)
                .orElse("Sin contenido");
    }
    
    public String getLastBlockHash(String blockchainId) {
        return blockRepository.findTopByBlockchainIdOrderByBlockIndexDesc(blockchainId)
                .map(Block::getCurrentHash)
                .orElse(Constants.GENESIS_PREV_HASH);
    }
    
    public Integer getNextBlockIndex(String blockchainId) {
        return blockRepository.findTopByBlockchainIdOrderByBlockIndexDesc(blockchainId)
                .map(block -> block.getBlockIndex() + 1)
                .orElse(0);
    }

    public Block mineAndSaveBlock(Block block, String content, String userId) {
        Block minedBlock = miningService.mineBlock(block, content, userId);
        Block savedBlock = blockRepository.save(minedBlock);
        return savedBlock;
    }

    public Block mineAndSaveBlock(Block block, String content) {
        return mineAndSaveBlock(block, content, null);
    }

    public Block getBlockById(String blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new RuntimeException("Bloque no encontrado con ID: " + blockId));
    }

    public boolean validateBlock(Block block) {
        if (block == null) {
            return false;
        }
        
        String currentHash = block.getCurrentHash();
        if (currentHash == null) {
            return false;
        }

        int difficulty = block.getDifficulty();
        String target = "0".repeat(difficulty);
        
        if (!currentHash.startsWith(target)) {
            return false;
        }

        String content = getBlockMainContent(block.getId());
        String regeneratedHash = generateBlockHash(block, content);
        
        return currentHash.equals(regeneratedHash);
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