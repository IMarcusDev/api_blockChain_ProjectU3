package com.proyecto.mjcd_software.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "blockchain_id")
    private String blockchainId;
    
    @Column(name = "block_index")
    @JsonProperty("index")
    private Integer blockIndex;
    
    @Column(name = "previous_hash")
    @JsonProperty("previousHash")
    private String previousHash;
    
    @Column(name = "current_hash")
    @JsonProperty("hash")
    private String currentHash;
    
    private Long nonce = 0L;
    
    @Column(name = "timestamp")
    private Long timestamp;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "block_type")
    private BlockType blockType;
    
    private Integer difficulty = 1;
    
    @Column(name = "is_valid")
    @JsonProperty("isValid")
    private Boolean isValid = true;
    
    @OneToMany(mappedBy = "blockId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BlockData> blockDataList;
    
    @Transient
    @JsonProperty("data")
    private String data;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public enum BlockType {
        GENESIS, TEXT, FILE, COMBINED
    }
}