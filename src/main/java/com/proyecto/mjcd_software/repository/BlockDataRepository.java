package com.proyecto.mjcd_software.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.mjcd_software.model.entity.BlockData;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockDataRepository extends JpaRepository<BlockData, String> {
    
    List<BlockData> findByBlockId(String blockId);
    
    Optional<BlockData> findByBlockIdAndDataType(String blockId, BlockData.DataType dataType);
    
    @Query("SELECT bd FROM BlockData bd WHERE bd.blockId = :blockId AND bd.dataType = 'TEXT'")
    Optional<BlockData> findTextDataByBlockId(String blockId);
    
    @Query("SELECT bd FROM BlockData bd WHERE bd.blockId = :blockId AND bd.dataType = 'FILE_HASH'")
    Optional<BlockData> findFileDataByBlockId(String blockId);
}