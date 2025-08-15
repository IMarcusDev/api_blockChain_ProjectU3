package com.proyecto.mjcd_software.security;

import com.proyecto.mjcd_software.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        
        logger.debug("Processing request: " + method + " " + requestURI);
        logger.debug("Authorization header: " + (authorizationHeader != null ? "Present" : "Not present"));
        
        String email = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwt);
                logger.debug("Extracted email from JWT: " + email);
            } catch (Exception e) {
                logger.error("Error extracting email from JWT: " + e.getMessage());
            }
        }
        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    request.setAttribute("userId", jwtUtil.extractUserId(jwt));
                    request.setAttribute("userEmail", email);
                    request.setAttribute("userFirstName", jwtUtil.extractFirstName(jwt));
                    request.setAttribute("userLastName", jwtUtil.extractLastName(jwt));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Successfully authenticated user: " + email);
                } else {
                    logger.debug("JWT token validation failed for user: " + email);
                }
            } catch (Exception e) {
                logger.error("Error validating JWT token: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equals(method)) {
            return true;
        }

        return path.startsWith("/api/v1/auth/") || 
               path.equals("/api/v1/blockchain/chain") ||
               path.equals("/api/v1/blockchain/stats") ||
               path.startsWith("/api/v1/validation/") ||
               path.equals("/api/v1/config") ||
               path.equals("/api/v1/users/points") ||
               path.equals("/api/v1/users/stats");
    }
}