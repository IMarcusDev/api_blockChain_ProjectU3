package com.proyecto.mjcd_software.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.mjcd_software.model.entity.Blockchain;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainRepository extends JpaRepository<Blockchain, String> {
    
    List<Blockchain> findByIsActiveTrue();
    
    Optional<Blockchain> findByNameAndIsActiveTrue(String name);
    
    @Query("SELECT b FROM Blockchain b WHERE b.isActive = true ORDER BY b.createdAt DESC")
    List<Blockchain> findActiveBlockchainsOrderByCreatedDesc();
    
    @Query("SELECT COUNT(bl) FROM Block bl WHERE bl.blockchainId = :blockchainId")
    Integer countBlocksByBlockchainId(String blockchainId);
}