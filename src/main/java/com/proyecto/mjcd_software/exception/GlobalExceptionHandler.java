package com.proyecto.mjcd_software.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BlockchainException.class)
    public ResponseEntity<Map<String, Object>> handleBlockchainException(BlockchainException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", e.getMessage(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "error", "Error interno del servidor",
            "details", e.getMessage(),
            "timestamp", System.currentTimeMillis()
        ));
    }
}