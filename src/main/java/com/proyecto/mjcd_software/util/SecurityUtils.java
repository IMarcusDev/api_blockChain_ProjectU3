package com.proyecto.mjcd_software.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtils {
    
    public static String getCurrentUserId() {
        try {
            ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            return (String) request.getAttribute("userId");
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getCurrentUserEmail() {
        try {
            ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            return (String) request.getAttribute("userEmail");
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isUserAuthenticated() {
        return getCurrentUserId() != null;
    }
}