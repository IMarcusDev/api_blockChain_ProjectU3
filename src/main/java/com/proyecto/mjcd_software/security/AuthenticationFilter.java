package com.proyecto.mjcd_software.security;

import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.model.entity.User;
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
import java.util.Base64;
import java.util.Optional;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath) || "OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            if (isTokenValid(token)) {
                String userId = extractUserIdFromToken(token);
                String email = extractEmailFromToken(token);

                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                request.setAttribute("userId", userId);
                request.setAttribute("userEmail", email);

                try {
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        request.setAttribute("userFirstName", user.getFirstName());
                        request.setAttribute("userLastName", user.getLastName());
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo obtener datos completos del usuario: " + e.getMessage());
                }

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("Error validando token: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isTokenValid(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            
            if (parts.length != 3) {
                return false;
            }

            String userId = parts[0];
            Optional<User> user = userRepository.findById(userId);
            
            return user.isPresent() && user.get().getIsActive();
        } catch (Exception e) {
            return false;
        }
    }

    private String extractUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return parts[0];
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    private String extractEmailFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return parts[1];
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    private boolean isPublicPath(String path) {
        String[] publicPaths = {
            "/api/v1/auth/",
            "/api/v1/blockchain/chain",
            "/api/v1/blockchain/stats",
            "/api/v1/blockchain/list",
            "/api/v1/validation/",
            "/api/v1/config",
            "/api/v1/users/points",
            "/api/v1/users/stats",
            "/api/v1/file/supported-types"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }
}