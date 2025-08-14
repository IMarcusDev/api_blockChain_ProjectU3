package com.proyecto.mjcd_software.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "block_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "block_id")
    private String blockId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "content_hash")
    private String contentHash;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_mime_type")
    private String fileMimeType;
    
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum DataType {
        TEXT, FILE_HASH, METADATA
    }
}