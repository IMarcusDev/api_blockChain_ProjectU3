package com.proyecto.mjcd_software.exception;

public class ValidationException extends RuntimeException {
    
    private String blockId;
    private Integer blockIndex;
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, String blockId) {
        super(message);
        this.blockId = blockId;
    }
    
    public ValidationException(String message, Integer blockIndex) {
        super(message);
        this.blockIndex = blockIndex;
    }
    
    public ValidationException(String message, String blockId, Integer blockIndex) {
        super(message);
        this.blockId = blockId;
        this.blockIndex = blockIndex;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getBlockId() {
        return blockId;
    }
    
    public Integer getBlockIndex() {
        return blockIndex;
    }
}