package com.proyecto.mjcd_software.model.dto.response;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class ValidationResponse {
    private Boolean isValid;
    private String message;
    private List<ValidationDetail> details;
    private Long timestamp;
    
    @Data
    @Builder
    public static class ValidationDetail {
        private Integer blockIndex;
        private String currentHash;
        private String previousHash;
        private String expectedPreviousHash;
        private String status;
        private String message;
    }
}