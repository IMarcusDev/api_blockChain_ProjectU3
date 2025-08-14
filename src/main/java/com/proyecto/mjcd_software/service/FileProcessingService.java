package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.mjcd_software.exception.BlockchainException;
import com.proyecto.mjcd_software.model.dto.request.FileUploadRequest;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.BlockData;
import com.proyecto.mjcd_software.repository.BlockDataRepository;
import com.proyecto.mjcd_software.util.HashGenerator;
import com.proyecto.mjcd_software.util.SecurityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileProcessingService {
    
    @Autowired
    private BlockService blockService;
    
    @Autowired
    private BlockDataRepository blockDataRepository;
    
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "application/pdf",
        "text/plain"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    public Block processFileUpload(FileUploadRequest request, String blockchainId) {
        MultipartFile file = request.getFile();
        String comment = request.getComment();
        validateFile(file);
        
        try {
            byte[] fileBytes = file.getBytes();
            String fileHash = HashGenerator.generateSHA256(new String(fileBytes));
            
            Block newBlock = createFileBlock(blockchainId, fileHash, comment);
            
            saveFileData(newBlock.getId(), file, fileHash, comment);
            
            return newBlock;
            
        } catch (IOException e) {
            throw new BlockchainException("Error al procesar el archivo: " + e.getMessage());
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BlockchainException("El archivo es obligatorio");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BlockchainException("El archivo excede el tamaño máximo permitido (10MB)");
        }
        
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BlockchainException("Tipo de archivo no permitido. Solo se permiten PDF y TXT");
        }
    }
    
    private String getUserFromRequest() {
        return SecurityUtils.getCurrentUserId();
    }

    private Block createFileBlock(String blockchainId, String fileHash, String comment) {
        String previousHash = blockService.getLastBlockHash(blockchainId);
        Integer nextIndex = blockService.getNextBlockIndex(blockchainId);
        
        Block newBlock = new Block();
        newBlock.setBlockchainId(blockchainId);
        newBlock.setBlockIndex(nextIndex);
        newBlock.setPreviousHash(previousHash);
        newBlock.setTimestamp(System.currentTimeMillis());
        
        if (comment != null && !comment.trim().isEmpty()) {
            newBlock.setBlockType(Block.BlockType.COMBINED);
        } else {
            newBlock.setBlockType(Block.BlockType.FILE);
        }
        
        String miningContent = fileHash;
        if (comment != null && !comment.trim().isEmpty()) {
            miningContent += ":" + comment;
        }

        return blockService.mineAndSaveBlock(newBlock, miningContent, getUserFromRequest());
    }
    
    private void saveFileData(String blockId, MultipartFile file, String fileHash, String comment) {
        BlockData fileData = new BlockData();
        fileData.setBlockId(blockId);
        fileData.setDataType(BlockData.DataType.FILE_HASH);
        fileData.setContent(fileHash);
        fileData.setContentHash(fileHash);
        fileData.setOriginalFilename(file.getOriginalFilename());
        fileData.setFileSize(file.getSize());
        fileData.setFileMimeType(file.getContentType());
        blockDataRepository.save(fileData);
        
        if (comment != null && !comment.trim().isEmpty()) {
            BlockData commentData = new BlockData();
            commentData.setBlockId(blockId);
            commentData.setDataType(BlockData.DataType.TEXT);
            commentData.setContent(comment);
            commentData.setContentHash(HashGenerator.generateSHA256(comment));
            blockDataRepository.save(commentData);
        }
    }
    
    public String extractTextFromFile(MultipartFile file) throws IOException {
        if ("text/plain".equals(file.getContentType())) {
            return new String(file.getBytes());
        } else if ("application/pdf".equals(file.getContentType())) {
            return "PDF_CONTENT_HASH_" + HashGenerator.generateSHA256(new String(file.getBytes()));
        }
        throw new BlockchainException("Tipo de archivo no soportado para extracción de texto");
    }
    
    public boolean isValidFileType(String contentType) {
        return ALLOWED_TYPES.contains(contentType);
    }
}