package com.proyecto.mjcd_software.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "blockchains")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blockchain {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String name;
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "creator_ip")
    private String creatorIp;
    
    @Column(name = "total_blocks")
    private Integer totalBlocks = 0;
    
    @OneToMany(mappedBy = "blockchainId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Block> blocks;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}