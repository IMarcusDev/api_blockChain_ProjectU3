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

@Service
@Transactional
public class UserPointsService {
    
    @Autowired
    private UserPointsRepository userPointsRepository;
    
    @Autowired
    private UserRepository userRepository;

    private static final String[] NAMES = {"Marcos", "Juan", "Carlos", "Mateo"};
    private static final String[] SURNAMES = {"Escobar", "Granda", "Ñato", "Sosa"};
    private static final String[] AVATAR_URLS = {
        "https://ui-avatars.com/api/?name=Marcos+Escobar&background=667eea&color=fff&size=40",
        "https://ui-avatars.com/api/?name=Juan+Granda&background=764ba2&color=fff&size=40",
        "https://ui-avatars.com/api/?name=Carlos+Nato&background=43e97b&color=fff&size=40",
        "https://ui-avatars.com/api/?name=Mateo+Sosa&background=f5576c&color=fff&size=40"
    };
    
    private final Random random = new Random();
    
    public List<Map<String, Object>> getAllUsersFormatted() {
        List<UserPoints> users = userPointsRepository.findAllByOrderByPointsDesc();
        
        return users.stream()
                .map(this::formatUserForFrontend)
                .collect(Collectors.toList());
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

    public List<UserPoints> generateRandomUsers(int count) {
        List<UserPoints> generatedUsers = new ArrayList<>();
        List<User> registeredUsers = userRepository.findByIsActiveTrueOrderByTotalPointsDesc();
        
        for (User user : registeredUsers) {
            if (generatedUsers.size() >= count) break;
            
            Optional<UserPoints> existingUserPoints = userPointsRepository.findByUser_Id(user.getId());
            if (existingUserPoints.isPresent()) {
                continue;
            }
            
            int nameIndex = random.nextInt(NAMES.length);
            String name = NAMES[nameIndex];
            String surname = SURNAMES[nameIndex];
            Integer points = random.nextInt(20) + 1;
            String chainHash = generateRandomHash();
            
            try {
                UserPoints userPoints = createUserPoints(name, surname, points, chainHash, user.getId());
                generatedUsers.add(userPoints);
            } catch (Exception e) {
                System.err.println("Error generando UserPoints para usuario " + user.getId() + ": " + e.getMessage());
            }
        }
        
        return generatedUsers;
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
    
    public void clearAllUsers() {
        userPointsRepository.deleteAll();
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
                    userPoints.setPoints(0);
                    userPoints.setAvatarUrl(user.getAvatarUrl());
                    userPoints.setChainHash(generateRandomHash());
                    calculateEfficiencyAndStatus(userPoints);
                    
                    return userPointsRepository.save(userPoints);
                });
    }
    
    private Map<String, Object> formatUserForFrontend(UserPoints user) {
        return Map.of(
            "id", user.getId(),
            "name", user.getUserName(),
            "surname", user.getUserSurname(),
            "point", user.getPoints(),
            "chain", user.getChainHash(),
            "avatar", user.getAvatarUrl(),
            "status", user.getStatus().name().toLowerCase(),
            "efficiency", user.getEfficiency().doubleValue(),
            "userId", user.getUserId(),
            "timestamp", user.getCreatedAt() != null ? 
                user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 
                System.currentTimeMillis()
        );
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

    private String generateRandomHash() {
        String randomData = "user_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
        return HashGenerator.generateSHA256(randomData);
    }

    private String generateAvatarUrl(String name, String surname) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name) && SURNAMES[i].equals(surname)) {
                return AVATAR_URLS[i];
            }
        }
        
        return String.format("https://ui-avatars.com/api/?name=%s+%s&background=667eea&color=fff&size=40", 
                name, surname);
    }

    public UserPoints findByNameAndSurname(String name, String surname) {
        return userPointsRepository.findByUserNameAndUserSurname(name, surname)
                .orElse(null);
    }
    
    public List<UserPoints> getUsersByStatus(UserPoints.Status status) {
        return userPointsRepository.findByStatus(status);
    }
}