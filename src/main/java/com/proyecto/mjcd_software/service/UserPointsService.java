package com.proyecto.mjcd_software.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.mjcd_software.model.entity.User;
import com.proyecto.mjcd_software.model.entity.UserPoints;
import com.proyecto.mjcd_software.repository.UserPointsRepository;
import com.proyecto.mjcd_software.repository.UserRepository;
import com.proyecto.mjcd_software.util.HashGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Transactional
public class UserPointsService {
    
    @Autowired
    private UserPointsRepository userPointsRepository;
    
    @Autowired
    private UserRepository userRepository;
    public List<Map<String, Object>> getAllUsersFormatted() {
        try {
            List<UserPoints> userPointsList = userPointsRepository.findAllByOrderByPointsDesc();
            
            if (!userPointsList.isEmpty()) {
                return userPointsList.stream()
                        .map(this::formatUserForFrontend)
                        .collect(Collectors.toList());
            }
            List<User> activeUsers = userRepository.findByIsActiveTrueOrderByTotalPointsDesc();
            List<Map<String, Object>> formattedUsers = new ArrayList<>();
            
            for (User user : activeUsers) {
                if (user.getTotalPoints() != null && user.getTotalPoints() > 0) {
                    
                    UserPoints userPoints = userPointsRepository.findByUser_Id(user.getId())
                            .orElse(null);
                    
                    if (userPoints == null) {
                        userPoints = new UserPoints();
                        userPoints.setUser(user);
                        userPoints.setUserName(user.getFirstName());
                        userPoints.setUserSurname(user.getLastName());
                        userPoints.setPoints(user.getTotalPoints());
                        userPoints.setAvatarUrl(user.getAvatarUrl());
                        userPoints.setChainHash(generateUserChainHash(user));
                        calculateEfficiencyAndStatus(userPoints);
                        userPoints = userPointsRepository.save(userPoints);
                    } else {
                        if (!userPoints.getPoints().equals(user.getTotalPoints())) {
                            userPoints.setPoints(user.getTotalPoints());
                            calculateEfficiencyAndStatus(userPoints);
                            userPoints = userPointsRepository.save(userPoints);
                        }
                    }
                    
                    formattedUsers.add(formatUserForFrontend(userPoints));
                }
            }
            
            formattedUsers.sort((a, b) -> {
                Integer pointsA = (Integer) a.get("point");
                Integer pointsB = (Integer) b.get("point");
                return pointsB.compareTo(pointsA);
            });
            
            return formattedUsers;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Transactional
    public void syncAllUserPoints() {
        List<User> activeUsers = userRepository.findByIsActiveTrueOrderByTotalPointsDesc();
        
        for (User user : activeUsers) {
            if (user.getTotalPoints() != null && user.getTotalPoints() > 0) {
                UserPoints userPoints = userPointsRepository.findByUser_Id(user.getId())
                        .orElse(null);
                
                if (userPoints == null) {
                    userPoints = new UserPoints();
                    userPoints.setUser(user);
                    userPoints.setUserName(user.getFirstName());
                    userPoints.setUserSurname(user.getLastName());
                    userPoints.setPoints(user.getTotalPoints());
                    userPoints.setAvatarUrl(user.getAvatarUrl());
                    userPoints.setChainHash(generateUserChainHash(user));
                    calculateEfficiencyAndStatus(userPoints);
                    userPointsRepository.save(userPoints);
                } else {
                    userPoints.setPoints(user.getTotalPoints());
                    userPoints.setUserName(user.getFirstName());
                    userPoints.setUserSurname(user.getLastName());
                    calculateEfficiencyAndStatus(userPoints);
                    userPointsRepository.save(userPoints);
                }
            }
        }
    }

    private Map<String, Object> formatUserForFrontend(UserPoints userPoints) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", userPoints.getId());
        formatted.put("name", userPoints.getUserName());
        formatted.put("surname", userPoints.getUserSurname());
        formatted.put("point", userPoints.getPoints());
        formatted.put("chain", userPoints.getChainHash());
        formatted.put("avatar", userPoints.getAvatarUrl());
        formatted.put("status", userPoints.getStatus().name().toLowerCase());
        formatted.put("efficiency", userPoints.getEfficiency().doubleValue());
        formatted.put("userId", userPoints.getUser() != null ? userPoints.getUser().getId() : null);
        formatted.put("timestamp", userPoints.getCreatedAt() != null ? 
            userPoints.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 
            System.currentTimeMillis());
        
        return formatted;
    }

    private void calculateEfficiencyAndStatus(UserPoints userPoints) {
        Integer points = userPoints.getPoints();
        double efficiency = Math.min(points * 10.0, 100.0);
        userPoints.setEfficiency(BigDecimal.valueOf(efficiency));

        if (points >= 10) {
            userPoints.setStatus(UserPoints.Status.HIGH);
        } else if (points >= 5) {
            userPoints.setStatus(UserPoints.Status.MEDIUM);
        } else {
            userPoints.setStatus(UserPoints.Status.LOW);
        }
    }

    private String generateUserChainHash(User user) {
        String data = user.getId() + user.getEmail() + System.currentTimeMillis();
        return HashGenerator.generateSHA256(data);
    }

    public UserPoints createUserPoints(String name, String surname, Integer points, String chainHash, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        Optional<UserPoints> existingUserPoints = userPointsRepository.findByUser_Id(userId);
        if (existingUserPoints.isPresent()) {
            throw new RuntimeException("Ya existe un registro de puntos para este usuario");
        }
        
        UserPoints userPoints = new UserPoints();
        userPoints.setUserName(name);
        userPoints.setUserSurname(surname);
        userPoints.setPoints(points != null ? points : random.nextInt(100) + 1);
        userPoints.setChainHash(chainHash != null ? chainHash : generateRandomHash());
        userPoints.setUser(user);

        calculateEfficiencyAndStatus(userPoints);
        
        userPoints.setAvatarUrl(generateAvatarUrl(name, surname));
        
        return userPointsRepository.save(userPoints);
    }

    public Map<String, Object> getUserStats() {
        Double avgPoints = userPointsRepository.findAveragePoints();
        Integer maxPoints = userPointsRepository.findMaxPoints();
        Long totalPoints = userPointsRepository.findTotalPoints();
        Long totalUsers = userPointsRepository.count();
        
        return Map.of(
            "totalUsers", totalUsers != null ? totalUsers : 0,
            "averagePoints", avgPoints != null ? avgPoints.intValue() : 0,
            "maxPoints", maxPoints != null ? maxPoints : 0,
            "totalPoints", totalPoints != null ? totalPoints : 0,
            "timestamp", System.currentTimeMillis()
        );
    }

    private final Random random = new Random();
    
    private String generateRandomHash() {
        String randomData = "user_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
        return HashGenerator.generateSHA256(randomData);
    }

    private String generateAvatarUrl(String name, String surname) {
        return String.format("https://ui-avatars.com/api/?name=%s+%s&background=667eea&color=fff&size=40", 
                name, surname);
    }

    public UserPoints findOrCreateUserPoints(String userId) {
        return userPointsRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user == null) return null;
                    
                    UserPoints userPoints = new UserPoints();
                    userPoints.setUser(user);
                    userPoints.setUserName(user.getFirstName());
                    userPoints.setUserSurname(user.getLastName());
                    userPoints.setPoints(user.getTotalPoints() != null ? user.getTotalPoints() : 0);
                    userPoints.setAvatarUrl(user.getAvatarUrl());
                    userPoints.setChainHash(generateRandomHash());
                    calculateEfficiencyAndStatus(userPoints);
                    
                    return userPointsRepository.save(userPoints);
                });
    }

    public UserPoints updateUserPoints(String userPointsId, Integer newPoints) {
        UserPoints userPoints = getUserById(userPointsId);
        userPoints.setPoints(newPoints);
        calculateEfficiencyAndStatus(userPoints);
        return userPointsRepository.save(userPoints);
    }

    public UserPoints getUserById(String userPointsId) {
        return userPointsRepository.findById(userPointsId)
                .orElseThrow(() -> new RuntimeException("UserPoints no encontrado con ID: " + userPointsId));
    }

    public void clearAllUsers() {
        userPointsRepository.deleteAll();
    }

    public List<UserPoints> generateRandomUsers(int count) {
        return new ArrayList<>();
    }
}