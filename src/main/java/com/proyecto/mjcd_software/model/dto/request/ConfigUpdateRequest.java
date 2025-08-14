package com.proyecto.mjcd_software.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class ConfigUpdateRequest {
    @Min(value = 1, message = "La dificultad mínima es 1")
    @Max(value = 10, message = "La dificultad máxima es 10")
    private Integer difficulty;
}