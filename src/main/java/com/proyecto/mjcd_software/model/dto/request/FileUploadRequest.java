package com.proyecto.mjcd_software.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private MultipartFile file;
    
    @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
    private String comment;
    
    private String blockchainId;
}