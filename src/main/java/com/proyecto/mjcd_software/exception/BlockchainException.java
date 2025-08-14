package com.proyecto.mjcd_software.exception;

public class BlockchainException extends RuntimeException {
    
    public BlockchainException(String message) {
        super(message);
    }
    
    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BlockchainException(Throwable cause) {
        super(cause);
    }
}