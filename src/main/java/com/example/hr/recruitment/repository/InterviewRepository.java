package com.example.hr.recruitment.repository;

import com.example.hr.recruitment.entity.Interview;
import com.example.hr.recruitment.entity.Candidate;
import com.example.hr.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Integer> {

    List<Interview> findByCandidateOrderByScheduledTimeDesc(Candidate candidate);

    List<Interview> findByInterviewerOrderByScheduledTimeDesc(User interviewer);

    List<Interview> findByStatusOrderByScheduledTimeDesc(String status);

    @Query("SELECT i FROM Interview i WHERE i.interviewer = :interviewer AND i.scheduledTime BETWEEN :startDate AND :endDate ORDER BY i.scheduledTime")
    List<Interview> findUpcomingInterviews(@Param("interviewer") User interviewer, 
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Interview i WHERE i.scheduledTime BETWEEN :startDate AND :endDate ORDER BY i.scheduledTime")
    List<Interview> findInterviewsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("""
           SELECT i FROM Interview i
           LEFT JOIN i.candidate c
           LEFT JOIN c.jobPosting j
           LEFT JOIN i.interviewer u
           WHERE (:keyword IS NULL OR :keyword = ''
                  OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:status IS NULL OR :status = '' OR i.status = :status)
             AND (:type IS NULL OR :type = '' OR i.interviewType = :type)
             AND (:startDate IS NULL OR i.scheduledTime >= :startDate)
             AND (:endDate IS NULL OR i.scheduledTime <= :endDate)
           """)
    Page<Interview> searchInterviews(@Param("keyword") String keyword,
                                     @Param("status") String status,
                                     @Param("type") String type,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    List<Interview> findByInterviewTypeAndStatusOrderByScheduledTimeDesc(String interviewType, String status);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.interviewer = :interviewer AND i.status = :status")
    long countByInterviewerAndStatus(@Param("interviewer") User interviewer, @Param("status") String status);

    long countByStatus(String status);

    @Query("SELECT i FROM Interview i WHERE i.candidate = :candidate AND i.interviewRound = :round")
    List<Interview> findByCandidateAndRound(@Param("candidate") Candidate candidate, @Param("round") Integer round);

    @Query("SELECT AVG(i.overallScore) FROM Interview i WHERE i.overallScore IS NOT NULL AND i.status = 'COMPLETED'")
    Double getAverageInterviewScore();

    @Query("SELECT i.recommendation, COUNT(i) FROM Interview i WHERE i.status = 'COMPLETED' GROUP BY i.recommendation")
    List<Object[]> getRecommendationStats();

    @Query("SELECT i.interviewType, COUNT(i) FROM Interview i GROUP BY i.interviewType")
    List<Object[]> getInterviewTypeStats();
}


