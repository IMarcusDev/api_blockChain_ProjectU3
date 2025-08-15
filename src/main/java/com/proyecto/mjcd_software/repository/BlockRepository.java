package com.proyecto.mjcd_software.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.mjcd_software.model.entity.Block;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, String> {
    
    List<Block> findByBlockchainIdOrderByBlockIndex(String blockchainId);
    
    Optional<Block> findTopByBlockchainIdOrderByBlockIndexDesc(String blockchainId);
    
    Optional<Block> findByBlockchainIdAndBlockIndex(String blockchainId, Integer blockIndex);
    
    @Query("SELECT b FROM Block b WHERE b.blockchainId = :blockchainId AND b.blockIndex = 0")
    Optional<Block> findGenesisBlock(@Param("blockchainId") String blockchainId);
    
    @Query("SELECT COUNT(b) FROM Block b WHERE b.blockchainId = :blockchainId")
    Integer countByBlockchainId(@Param("blockchainId") String blockchainId);
    
    @Query("SELECT b FROM Block b WHERE b.blockchainId = :blockchainId AND b.isValid = false")
    List<Block> findInvalidBlocksByBlockchainId(@Param("blockchainId") String blockchainId);

    @Query("SELECT b FROM Block b WHERE b.minedBy = :userId ORDER BY b.blockIndex DESC")
    List<Block> findByMinedByOrderByBlockIndexDesc(@Param("userId") String userId);
    
    @Query("SELECT b FROM Block b ORDER BY b.blockIndex DESC")
    List<Block> findTopByOrderByBlockIndexDesc();
    
    @Query("SELECT b FROM Block b WHERE b.minedBy = :userId ORDER BY b.timestamp DESC")
    List<Block> findByMinedByOrderByTimestampDesc(@Param("userId") String userId);
}