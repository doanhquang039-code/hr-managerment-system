package com.example.hr.engagement.repository;

import com.example.hr.engagement.entity.PulseSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PulseSurveyRepository extends JpaRepository<PulseSurvey, Integer> {
    
    List<PulseSurvey> findByIsActiveTrue();
    
    long countByIsActiveTrue();
    
    @Query("SELECT s FROM PulseSurvey s WHERE s.isActive = true AND :date BETWEEN s.startDate AND s.endDate")
    List<PulseSurvey> findActiveSurveys(@Param("date") LocalDate date);
    
    List<PulseSurvey> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}


