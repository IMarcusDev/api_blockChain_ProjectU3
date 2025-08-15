// AuthController.java - Agregar estas anotaciones a TODOS los controladores
package com.proyecto.mjcd_software.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.mjcd_software.model.dto.request.LoginRequest;
import com.proyecto.mjcd_software.model.dto.request.RegisterRequest;
import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.service.AuthService;
import com.proyecto.mjcd_software.service.AuthService.LoginResponse;
import com.proyecto.mjcd_software.exception.BlockchainException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("AuthController - Login request received: " + request.getEmail());
            
            LoginResponse loginResponse = authService.login(request);
            User user = loginResponse.getUser();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login exitoso",
                "token", loginResponse.getToken(),
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                    "totalPoints", user.getTotalPoints(),
                    "blocksMined", user.getBlocksMined()
                )
            ));
            
        } catch (BlockchainException e) {
            System.out.println("AuthController - Login error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            System.out.println("AuthController - Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Error interno del servidor"
            ));
        }
    }
    
    @PostMapping("/register")
    
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario registrado exitosamente",
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName()
                )
            ));
            
        } catch (BlockchainException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/me")
    
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String email = (String) request.getAttribute("userEmail");
            String firstName = (String) request.getAttribute("userFirstName");
            String lastName = (String) request.getAttribute("userLastName");
            
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Token requerido"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", userId,
                    "email", email,
                    "firstName", firstName,
                    "lastName", lastName
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "error", "Token inválido"
            ));
        }
    }
}