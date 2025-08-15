package com.proyecto.mjcd_software.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.mjcd_software.model.entity.UserPoints;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPointsRepository extends JpaRepository<UserPoints, String> {
    
    List<UserPoints> findAllByOrderByPointsDesc();
    
    Optional<UserPoints> findByUserNameAndUserSurname(String userName, String userSurname);

    Optional<UserPoints> findByUser_Id(String userId);
    
    List<UserPoints> findByStatus(UserPoints.Status status);
    
    @Query("SELECT AVG(u.points) FROM UserPoints u")
    Double findAveragePoints();
    
    @Query("SELECT MAX(u.points) FROM UserPoints u")
    Integer findMaxPoints();
    
    @Query("SELECT SUM(u.points) FROM UserPoints u")
    Long findTotalPoints();
    
    @Query("SELECT u FROM UserPoints u ORDER BY u.points DESC")
    List<UserPoints> findTopUsersByPoints();
}