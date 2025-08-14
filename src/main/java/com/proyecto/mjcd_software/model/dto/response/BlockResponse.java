package com.proyecto.mjcd_software.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class BlockResponse {
    @JsonProperty("index")
    private Integer index;
    
    private String data;
    
    @JsonProperty("previousHash")
    private String previousHash;
    
    private String hash;    
    
    private Long nonce;
    
    private Long timestamp;
    
    @JsonProperty("isValid")
    private Boolean isValid;
    
    private String blockType;
}