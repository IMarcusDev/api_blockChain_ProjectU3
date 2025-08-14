package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.dto.request.LoginRequest;
import com.proyecto.mjcd_software.model.dto.request.RegisterRequest;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.util.JwtUtil;
import com.proyecto.mjcd_software.exception.BlockchainException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(request.getEmail());
        
        if (userOpt.isEmpty()) {
            throw new BlockchainException("Usuario no encontrado o inactivo");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BlockchainException("Credenciales inválidas");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName()
        );
        
        return new LoginResponse(user, token);
    }
    
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BlockchainException("El email ya está registrado");
        }
        
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setAvatarUrl(generateAvatarUrl(request.getFirstName(), request.getLastName()));
        
        return userRepository.save(newUser);
    }
    
    public User getCurrentUser(String token) {
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new BlockchainException("Usuario no encontrado"));
    }
    
    private String generateAvatarUrl(String firstName, String lastName) {
        return String.format("https://ui-avatars.com/api/?name=%s+%s&background=667eea&color=fff&size=40", 
                firstName, lastName);
    }

    public static class LoginResponse {
        private User user;
        private String token;
        
        public LoginResponse(User user, String token) {
            this.user = user;
            this.token = token;
        }
        
        public User getUser() { return user; }
        public String getToken() { return token; }
    }
}