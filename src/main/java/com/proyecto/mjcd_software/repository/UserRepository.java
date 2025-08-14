package com.proyecto.mjcd_software.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.mjcd_software.model.entity.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    List<User> findByIsActiveTrueOrderByTotalPointsDesc();
    
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.blocksMined DESC")
    List<User> findTopMiners();
    
    @Query("SELECT SUM(u.totalPoints) FROM User u WHERE u.isActive = true")
    Long getTotalPointsInSystem();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long getActiveUsersCount();
}