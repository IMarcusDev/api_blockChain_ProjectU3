package com.proyecto.mjcd_software.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es obligatorio")
    private String email;
    
    @NotBlank(message = "Password es obligatorio")
    @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 50, message = "Nombre no puede exceder 50 caracteres")
    private String firstName;
    
    @NotBlank(message = "Apellido es obligatorio")
    @Size(max = 50, message = "Apellido no puede exceder 50 caracteres")
    private String lastName;
}