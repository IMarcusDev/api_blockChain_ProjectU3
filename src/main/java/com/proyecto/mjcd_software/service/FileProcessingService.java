package com.proyecto.mjcd_software.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.mjcd_software.exception.BlockchainException;
import com.proyecto.mjcd_software.model.dto.request.FileUploadRequest;
import com.proyecto.mjcd_software.model.entity.Block;
import com.proyecto.mjcd_software.model.entity.BlockData;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.repository.BlockDataRepository;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.repository.UserPointsRepository;
import com.proyecto.mjcd_software.util.HashGenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional
public class FileProcessingService {
    
    @Autowired
    private BlockService blockService;
    
    @Autowired
    private BlockDataRepository blockDataRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserPointsRepository userPointsRepository;
    
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "application/pdf",
        "text/plain"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public Block processFileUpload(FileUploadRequest request, String blockchainId, String userId) {
        MultipartFile file = request.getFile();
        String comment = request.getComment();
        
        log.info("Procesando archivo: {} ({}) para usuario: {}", 
                file.getOriginalFilename(), file.getContentType(), userId);
        log.info("Comentario incluido: {}", comment != null && !comment.trim().isEmpty() ? "Sí" : "No");
        
        validateFile(file);
        
        try {
            byte[] fileBytes = file.getBytes();
            String fileHash = HashGenerator.generateSHA256(new String(fileBytes));
            
            log.debug("Hash del archivo calculado: {}", fileHash);

            int pointsToEarn = calculatePointsForUpload(file, comment);
            log.info("Puntos a ganar por esta subida: {}", pointsToEarn);
            
            Block newBlock = createFileBlock(blockchainId, fileHash, comment, userId);
            
            saveFileData(newBlock.getId(), file, fileHash, comment);

            rewardUserForUpload(userId, newBlock, pointsToEarn, file, comment);
            
            log.info("Archivo procesado exitosamente en bloque: {} con {} puntos otorgados", 
                    newBlock.getId(), pointsToEarn);
            return newBlock;
            
        } catch (IOException e) {
            log.error("Error al procesar archivo {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new BlockchainException("Error al procesar el archivo: " + e.getMessage());
        }
    }
    
    private int calculatePointsForUpload(MultipartFile file, String comment) {
        int points = 0;

        if (file != null && !file.isEmpty()) {
            points += 1;
            log.debug("Archivo presente: +1 punto");
        }

        if (comment != null && !comment.trim().isEmpty()) {
            points += 1;
            log.debug("Comentario presente: +1 punto");
        }
        
        log.info("Total de puntos calculados: {}", points);
        return points;
    }
    
    @Transactional
    private void rewardUserForUpload(String userId, Block block, int points, MultipartFile file, String comment) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || !user.getIsActive()) {
                log.warn("Usuario {} no encontrado o inactivo para otorgar recompensa de subida", userId);
                return;
            }

            user.setTotalPoints(user.getTotalPoints() + points);
            userRepository.save(user);

            UserPoints userPoints = userPointsRepository.findByUser_Id(userId).orElse(null);
            
            if (userPoints == null) {
                userPoints = new UserPoints();
                userPoints.setUser(user);
                userPoints.setUserName(user.getFirstName());
                userPoints.setUserSurname(user.getLastName());
                userPoints.setPoints(points);
                userPoints.setAvatarUrl(user.getAvatarUrl());
                userPoints.setBlockchainId(block.getBlockchainId());
                userPoints.setChainHash(generateUserChainHash(user, block));
                
                log.info("Creado nuevo registro UserPoints para {} con {} puntos por subida de archivo", 
                        user.getFirstName(), points);
            } else {
                userPoints.setPoints(userPoints.getPoints() + points);
                userPoints.setChainHash(generateUserChainHash(user, block));
                
                log.info("Actualizado UserPoints para {}. Puntos agregados: {} (Total: {})", 
                        user.getFirstName(), points, userPoints.getPoints());
            }

            calculateEfficiencyAndStatus(userPoints);
            userPointsRepository.save(userPoints);

            String activity = buildActivityDescription(file, comment);
            log.info("Recompensa de subida otorgada - Usuario: {} | Actividad: {} | Puntos: {} | Total: {}", 
                    user.getFirstName(), activity, points, user.getTotalPoints());
            
        } catch (Exception e) {
            log.error("Error al otorgar recompensa de subida al usuario {}: {}", userId, e.getMessage());
        }
    }
    
    private String buildActivityDescription(MultipartFile file, String comment) {
        StringBuilder activity = new StringBuilder();
        
        if (file != null && !file.isEmpty()) {
            activity.append("Archivo: ").append(file.getOriginalFilename());
        }
        
        if (comment != null && !comment.trim().isEmpty()) {
            if (activity.length() > 0) activity.append(" + ");
            activity.append("Texto/Comentario");
        }
        
        return activity.toString();
    }
    
    private void calculateEfficiencyAndStatus(UserPoints userPoints) {
        Integer points = userPoints.getPoints();

        double efficiency = Math.min(points * 10.0, 100.0);
        userPoints.setEfficiency(java.math.BigDecimal.valueOf(efficiency));

        if (points >= 10) {
            userPoints.setStatus(UserPoints.Status.HIGH);
        } else if (points >= 5) {
            userPoints.setStatus(UserPoints.Status.MEDIUM);
        } else {
            userPoints.setStatus(UserPoints.Status.LOW);
        }
    }
    
    private String generateUserChainHash(User user, Block block) {
        String data = user.getId() + user.getEmail() + block.getCurrentHash() + System.currentTimeMillis();
        return HashGenerator.generateSHA256(data);
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BlockchainException("El archivo es obligatorio");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Archivo rechazado por tamaño: {} bytes (máximo: {})", 
                    file.getSize(), MAX_FILE_SIZE);
            throw new BlockchainException("El archivo excede el tamaño máximo permitido (10MB)");
        }
        
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            log.warn("Tipo de archivo rechazado: {}", contentType);
            throw new BlockchainException("Tipo de archivo no permitido. Solo se permiten PDF y TXT");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new BlockchainException("El archivo debe tener un nombre válido");
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BlockchainException("Nombre de archivo no válido");
        }
        
        log.debug("Archivo validado exitosamente: {} ({} bytes)", filename, file.getSize());
    }

    private Block createFileBlock(String blockchainId, String fileHash, String comment, String userId) {
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

        return blockService.mineAndSaveBlock(newBlock, miningContent, userId);
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
            commentData.setContent(comment.trim());
            commentData.setContentHash(HashGenerator.generateSHA256(comment.trim()));
            blockDataRepository.save(commentData);
        }
        
        log.debug("Datos del archivo guardados para bloque: {}", blockId);
    }
    
    public String extractTextFromFile(MultipartFile file) throws IOException {
        validateFile(file);
        
        String contentType = file.getContentType();
        log.debug("Extrayendo texto de archivo tipo: {}", contentType);
        
        if ("text/plain".equals(contentType)) {
            String content = new String(file.getBytes());
            log.debug("Texto extraído: {} caracteres", content.length());
            return content;
        } else if ("application/pdf".equals(contentType)) {
            String pdfHash = "PDF_CONTENT_HASH_" + HashGenerator.generateSHA256(new String(file.getBytes()));
            log.debug("Hash PDF generado: {}", pdfHash);
            return pdfHash;
        }
        
        throw new BlockchainException("Tipo de archivo no soportado para extracción de texto");
    }
    
    public boolean isValidFileType(String contentType) {
        boolean isValid = ALLOWED_TYPES.contains(contentType);
        log.debug("Validación de tipo de archivo {}: {}", contentType, isValid);
        return isValid;
    }
}