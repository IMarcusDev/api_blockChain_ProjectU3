package com.proyecto.mjcd_software.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.proyecto.mjcd_software.exception.BlockchainException;

public class SHA256Util {
    
    private static final String ALGORITHM = "SHA-256";
    
    public static String generateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("Error al generar hash SHA-256: " + e.getMessage());
        }
    }
    
    public static String generateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("Error al generar hash SHA-256: " + e.getMessage());
        }
    }
    
    public static String generateHashWithSeed(String content, String seed, String timestamp) {
        String data = content + seed + timestamp;
        return generateHash(data);
    }

    public static String generateBlockHash(Integer id, String previousHash, Long timestamp, String content, Long nonce) {
        String blockData = String.format("{id:%d,hashPrevio:%s,timeStamp:%d,contenido:%s,nonce:%d}",
                id,
                previousHash != null ? previousHash : "null",
                timestamp,
                content,
                nonce);
        return generateHash(blockData);
    }

    public static boolean hasRequiredDifficulty(String hash, int difficulty) {
        if (hash == null || hash.length() < difficulty) {
            return false;
        }
        
        String requiredPrefix = "0".repeat(difficulty);
        return hash.startsWith(requiredPrefix);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public static boolean isValidSHA256(String hash) {
        if (hash == null) {
            return false;
        }
        return hash.matches("^[a-f0-9]{64}$");
    }
    
    public static String generateDoubleHash(String data) {
        String firstHash = generateHash(data);
        return generateHash(firstHash);
    }
}