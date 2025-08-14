package com.proyecto.mjcd_software.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBlockRequest {
    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 1000, message = "El contenido no puede exceder 1000 caracteres")
    private String content;
    
    private String blockchainId;
}