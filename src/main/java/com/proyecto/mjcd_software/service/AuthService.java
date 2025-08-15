package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.dto.request.LoginRequest;
import com.proyecto.mjcd_software.model.dto.request.RegisterRequest;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.exception.BlockchainException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;

    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @Autowired
    public void setPasswordEncoder(org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(request.getEmail());
        
        if (userOpt.isEmpty()) {
            throw new BlockchainException("Usuario no encontrado");
        }
        
        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BlockchainException("Contraseña incorrecta");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = generateSimpleToken(user);
        
        return new LoginResponse(user, token);
    }
    
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BlockchainException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setAvatarUrl(generateAvatarUrl(request.getFirstName(), request.getLastName()));
        newUser.setIsActive(true);
        newUser.setTotalPoints(0);
        newUser.setBlocksMined(0);
        
        return userRepository.save(newUser);
    }
    
    public User getCurrentUser(String token) {
        String userId = extractUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BlockchainException("Usuario no encontrado"));
    }

    private String generateSimpleToken(User user) {
        String payload = user.getId() + ":" + user.getEmail() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(payload.getBytes());
    }

    public String extractUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return parts[0];
        } catch (Exception e) {
            throw new BlockchainException("Token inválido");
        }
    }
    
    // Extraer email del token
    public String extractEmailFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return parts[1];
        } catch (Exception e) {
            throw new BlockchainException("Token inválido");
        }
    }
    
    private String generateAvatarUrl(String firstName, String lastName) {
        return String.format(
            "https://ui-avatars.com/api/?name=%s+%s&background=667eea&color=fff&size=40", 
            firstName, lastName
        );
    }

    public static class LoginResponse {
        private User user;
        private String token;
        
        public LoginResponse(User user, String token) {
            this.user = user;
            this.token = token;
        }
        
        public User getUser() { 
            return user; 
        }
        
        public String getToken() { 
            return token; 
        }
    }
}