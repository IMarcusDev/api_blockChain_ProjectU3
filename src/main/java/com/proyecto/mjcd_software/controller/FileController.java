package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.mjcd_software.model.dto.request.FileUploadRequest;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.FileProcessingService;
import com.proyecto.mjcd_software.exception.BlockchainException;
import com.proyecto.mjcd_software.util.SecurityUtils;

import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {
    
    @Autowired
    private FileProcessingService fileProcessingService;
    
    @Autowired
    private BlockchainService blockchainService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "blockchainId", required = false) String blockchainId) {

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        
        try {
            if (blockchainId == null) {
                Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
                blockchainId = defaultBlockchain.getId();
            }
            
            FileUploadRequest request = new FileUploadRequest();
            request.setFile(file);
            request.setComment(comment);
            request.setBlockchainId(blockchainId);
            
            Block newBlock = fileProcessingService.processFileUpload(request, blockchainId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Archivo procesado y bloque creado exitosamente",
                "blockId", newBlock.getId(),
                "blockIndex", newBlock.getBlockIndex(),
                "hash", newBlock.getCurrentHash(),
                "nonce", newBlock.getNonce(),
                "filename", file.getOriginalFilename(),
                "fileSize", file.getSize(),
                "blockType", newBlock.getBlockType().name().toLowerCase(),
                "timestamp", newBlock.getTimestamp()
            ));
            
        } catch (BlockchainException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateFile(@RequestParam("file") MultipartFile file) {
        boolean isValid = fileProcessingService.isValidFileType(file.getContentType());
        
        return ResponseEntity.ok(Map.of(
            "isValid", isValid,
            "filename", file.getOriginalFilename(),
            "contentType", file.getContentType(),
            "size", file.getSize()
        ));
    }

    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedFileTypes() {
        return ResponseEntity.ok(Map.of(
            "supportedTypes", new String[]{"application/pdf", "text/plain"},
            "maxSize", "10MB",
            "description", "Se soportan archivos PDF y TXT con un tamaño máximo de 10MB"
        ));
    }

    @PostMapping("/extract-text")
    public ResponseEntity<Map<String, Object>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String extractedText = fileProcessingService.extractTextFromFile(file);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filename", file.getOriginalFilename(),
                "extractedText", extractedText,
                "contentType", file.getContentType()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al extraer texto: " + e.getMessage()
            ));
        }
    }
}