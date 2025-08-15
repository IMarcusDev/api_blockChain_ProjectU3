package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.mjcd_software.model.dto.request.FileUploadRequest;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.Blockchain;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.repository.UserPointsRepository;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.service.BlockchainService;
import com.proyecto.mjcd_software.service.FileProcessingService;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileProcessingService fileProcessingService;
    
    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPointsRepository userPointsRepository;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "blockchainId", required = false) String blockchainId,
            HttpServletRequest httpRequest) {

        String currentUserId = getCurrentUserId(httpRequest);
        
        try {
            if (blockchainId == null) {
                Blockchain defaultBlockchain = blockchainService.getDefaultBlockchain();
                blockchainId = defaultBlockchain.getId();
            }

            int expectedPoints = 0;
            boolean hasFile = file != null && !file.isEmpty();
            boolean hasComment = comment != null && !comment.trim().isEmpty();
            
            if (hasFile) expectedPoints += 1;
            if (hasComment) expectedPoints += 1;
            
            log.info("Procesando subida - Archivo: {}, Comentario: {}, Puntos esperados: {}", 
                    hasFile, hasComment, expectedPoints);
            
            FileUploadRequest request = new FileUploadRequest();
            request.setFile(file);
            request.setComment(comment);
            request.setBlockchainId(blockchainId);

            Block newBlock = fileProcessingService.processFileUpload(request, blockchainId, currentUserId);

            User updatedUser = userRepository.findById(currentUserId).orElse(null);
            UserPoints updatedUserPoints = userPointsRepository.findByUser_Id(currentUserId).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Archivo procesado exitosamente. Has ganado %d punto(s)!", expectedPoints));
            response.put("blockId", newBlock.getId());
            response.put("blockIndex", newBlock.getBlockIndex());
            response.put("hash", newBlock.getCurrentHash());
            response.put("nonce", newBlock.getNonce());
            response.put("filename", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("blockType", newBlock.getBlockType().name().toLowerCase());
            response.put("timestamp", newBlock.getTimestamp());
            response.put("pointsEarned", expectedPoints);
            
            Map<String, Object> pointsBreakdown = new HashMap<>();
            pointsBreakdown.put("filePoints", hasFile ? 1 : 0);
            pointsBreakdown.put("commentPoints", hasComment ? 1 : 0);
            pointsBreakdown.put("total", expectedPoints);
            response.put("pointsBreakdown", pointsBreakdown);

            Map<String, Object> userStats = new HashMap<>();
            userStats.put("totalPoints", updatedUser != null ? updatedUser.getTotalPoints() : 0);
            userStats.put("blocksMined", updatedUser != null ? updatedUser.getBlocksMined() : 0);
            userStats.put("userPointsTotal", updatedUserPoints != null ? updatedUserPoints.getPoints() : 0);
            userStats.put("efficiency", updatedUserPoints != null ? updatedUserPoints.getEfficiency().doubleValue() : 0.0);
            userStats.put("status", updatedUserPoints != null ? updatedUserPoints.getStatus().name().toLowerCase() : "low");
            response.put("userStats", userStats);
            
            return ResponseEntity.ok(response);
            
        } catch (BlockchainException e) {
            log.error("Error de blockchain en subida de archivo: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error interno en subida de archivo: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateFile(@RequestParam("file") MultipartFile file) {
        boolean isValid = fileProcessingService.isValidFileType(file.getContentType());
        
        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);
        response.put("filename", file.getOriginalFilename());
        response.put("contentType", file.getContentType());
        response.put("size", file.getSize());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedFileTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("supportedTypes", new String[]{"application/pdf", "text/plain"});
        response.put("maxSize", "10MB");
        response.put("description", "Se soportan archivos PDF y TXT con un tamaño máximo de 10MB");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/extract-text")
    public ResponseEntity<Map<String, Object>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String extractedText = fileProcessingService.extractTextFromFile(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", file.getOriginalFilename());
            response.put("extractedText", extractedText);
            response.put("contentType", file.getContentType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al extraer texto: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String getCurrentUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            throw new BlockchainException("Usuario no autenticado");
        }
        return userId;
    }
}