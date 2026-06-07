package com.example.hr.recruitment.repository;

import com.example.hr.recruitment.entity.Candidate;
import com.example.hr.recruitment.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    List<Candidate> findByJobPostingOrderByAppliedAtDesc(JobPosting jobPosting);

    List<Candidate> findByCurrentStageOrderByAppliedAtDesc(String currentStage);

    List<Candidate> findByJobPostingAndCurrentStageOrderByAppliedAtDesc(JobPosting jobPosting, String currentStage);

    @Query("SELECT c FROM Candidate c WHERE c.fullName LIKE %:keyword% OR c.email LIKE %:keyword% OR c.skills LIKE %:keyword%")
    List<Candidate> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Candidate c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:stage IS NULL OR :stage = '' OR c.currentStage = :stage)")
    org.springframework.data.domain.Page<Candidate> searchCandidates(@Param("keyword") String keyword, @Param("stage") String stage, org.springframework.data.domain.Pageable pageable);

    List<Candidate> findBySourceOrderByAppliedAtDesc(String source);

    @Query("SELECT c FROM Candidate c WHERE c.overallScore >= :minScore ORDER BY c.overallScore DESC")
    List<Candidate> findByMinScore(@Param("minScore") Integer minScore);

    @Query("SELECT c FROM Candidate c WHERE c.yearsOfExperience >= :minExperience ORDER BY c.yearsOfExperience DESC")
    List<Candidate> findByMinExperience(@Param("minExperience") Integer minExperience);

    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.currentStage = :stage")
    long countByStage(@Param("stage") String stage);

    @Query("SELECT c FROM Candidate c WHERE c.appliedAt BETWEEN :startDate AND :endDate ORDER BY c.appliedAt DESC")
    List<Candidate> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.currentStage, COUNT(c) FROM Candidate c GROUP BY c.currentStage")
    List<Object[]> getCandidatesByStage();

    @Query("SELECT c.source, COUNT(c) FROM Candidate c GROUP BY c.source")
    List<Object[]> getCandidatesBySource();

    @Query("SELECT AVG(c.overallScore) FROM Candidate c WHERE c.overallScore IS NOT NULL")
    Double getAverageScore();

    // Additional methods for compatibility with existing controllers
    long countByCurrentStage(String currentStage);
    
    List<Candidate> findByCurrentStage(String currentStage);
    
    List<Candidate> findByFullNameContainingIgnoreCase(String fullName);
    
    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.appliedAt > :date")
    long countByAppliedDateAfter(@Param("date") java.time.LocalDate date);
}

