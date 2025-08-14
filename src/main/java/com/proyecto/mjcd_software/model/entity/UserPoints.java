package com.proyecto.mjcd_software.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPoints {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "user_surname")
    private String userSurname;
    
    private Integer points = 0;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal efficiency = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.MEDIUM;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "blockchain_id")
    private String blockchainId;
    
    @Column(name = "chain_hash")
    private String chainHash;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        HIGH, MEDIUM, LOW
    }
}