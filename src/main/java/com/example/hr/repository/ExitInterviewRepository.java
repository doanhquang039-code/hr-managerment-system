package com.example.hr.repository;

import com.example.hr.models.ExitInterview;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExitInterviewRepository extends JpaRepository<ExitInterview, Integer> {
    
    Optional<ExitInterview> findByUser(User user);
    
    List<ExitInterview> findByInterviewer(User interviewer);
    
    List<ExitInterview> findByInterviewDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT AVG(e.satisfactionRating) FROM ExitInterview e WHERE e.satisfactionRating IS NOT NULL")
    Double getAverageSatisfactionRating();
    
    long countByWouldRecommend(boolean wouldRecommend);
}


