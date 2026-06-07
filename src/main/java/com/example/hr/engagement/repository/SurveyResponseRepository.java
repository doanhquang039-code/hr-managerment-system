package com.example.hr.engagement.repository;

import com.example.hr.engagement.entity.PulseSurvey;
import com.example.hr.engagement.entity.SurveyResponse;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Integer> {
    
    List<SurveyResponse> findBySurvey(PulseSurvey survey);
    
    List<SurveyResponse> findByUser(User user);
    
    Optional<SurveyResponse> findByUserAndSurvey(User user, PulseSurvey survey);
    
    boolean existsByUserAndSurvey(User user, PulseSurvey survey);
    
    long countBySurvey(PulseSurvey survey);
}


