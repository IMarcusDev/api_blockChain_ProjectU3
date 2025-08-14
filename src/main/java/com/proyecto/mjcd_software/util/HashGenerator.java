package com.proyecto.mjcd_software.util;

import java.security.MessageDigest;

public class HashGenerator {
    
    public static String generateSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating SHA-256 hash", e);
        }
    }

    public static String generateHashWithSeed(String content, String seed, String timestamp) {
        String data = content + seed + timestamp;
        return generateSHA256(data);
    }
}