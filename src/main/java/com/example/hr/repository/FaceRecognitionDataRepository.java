package com.example.hr.repository;

import com.example.hr.models.FaceRecognitionData;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceRecognitionDataRepository extends JpaRepository<FaceRecognitionData, Integer> {
    
    List<FaceRecognitionData> findByUser(User user);
    
    List<FaceRecognitionData> findByUserAndIsActiveTrue(User user);
    
    Optional<FaceRecognitionData> findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);
    
    boolean existsByUserAndIsActiveTrue(User user);
}


