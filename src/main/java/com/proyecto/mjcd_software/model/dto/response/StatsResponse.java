package com.proyecto.mjcd_software.model.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class StatsResponse {
    private Integer totalBlocks;
    private Integer totalTransactions;
    private Long oldestTime;
    private Long newestTime;
    private Double averageBlockTime;
    private Integer chainIntegrity;

    private Integer totalUsers;
    private Double averagePoints;
    private Integer maxPoints;
    private Long totalPoints;
    
    private Long timestamp;
}