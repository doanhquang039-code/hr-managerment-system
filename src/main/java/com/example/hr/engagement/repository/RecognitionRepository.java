package com.example.hr.engagement.repository;

import com.example.hr.engagement.entity.Recognition;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecognitionRepository extends JpaRepository<Recognition, Integer> {

    @Query("SELECT r FROM Recognition r LEFT JOIN FETCH r.recipient LEFT JOIN FETCH r.giver ORDER BY r.createdAt DESC")
    List<Recognition> findAllWithUsers();

    @Query("SELECT r FROM Recognition r LEFT JOIN FETCH r.recipient LEFT JOIN FETCH r.giver WHERE r.type = :type ORDER BY r.createdAt DESC")
    List<Recognition> findByTypeWithUsers(@Param("type") String type);
    
    List<Recognition> findByRecipient(User recipient);
    
    List<Recognition> findByGiver(User giver);
    
    List<Recognition> findByType(String type);
    
    List<Recognition> findByIsPublicTrueOrderByCreatedAtDesc();
    
    @Query("SELECT SUM(r.points) FROM Recognition r WHERE r.recipient = :user")
    Integer getTotalPointsByRecipient(@Param("user") User user);
    
    @Query("SELECT r FROM Recognition r WHERE r.isPublic = true ORDER BY r.createdAt DESC")
    List<Recognition> findRecentPublicRecognitions();
    
    @Query("SELECT COUNT(r) FROM Recognition r WHERE MONTH(r.createdAt) = MONTH(CURRENT_DATE) AND YEAR(r.createdAt) = YEAR(CURRENT_DATE)")
    long countThisMonth();
    
    @Query("SELECT SUM(r.points) FROM Recognition r")
    Integer getTotalPoints();
    
    long countByRecipient(User recipient);
}


