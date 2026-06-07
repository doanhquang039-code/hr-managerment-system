package com.example.hr.training.repository;

import com.example.hr.enums.EnrollmentStatus;
import com.example.hr.training.entity.TrainingEnrollment;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, Integer> {
    
    @Query("SELECT COUNT(e) FROM TrainingEnrollment e WHERE e.status = :status")
    long countByStatusString(@Param("status") String status);

    @Query("SELECT COUNT(e) FROM TrainingEnrollment e WHERE e.user = :user AND e.status = :status")
    long countByUserAndStatusString(@Param("user") User user, @Param("status") String status);
    List<TrainingEnrollment> findByUserOrderByEnrolledAtDesc(User user);
    
    // Methods needed by TrainingService
    List<TrainingEnrollment> findByProgramId(Integer programId);
    
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM TrainingEnrollment e WHERE e.user.id = :userId AND e.program.id = :programId")
    boolean existsByUserIdAndProgramId(@Param("userId") Integer userId, @Param("programId") Integer programId);
    
    @Query("SELECT e FROM TrainingEnrollment e WHERE e.user.id = :userId ORDER BY e.enrolledAt DESC")
    List<TrainingEnrollment> findByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT e FROM TrainingEnrollment e WHERE e.user.id = :userId AND e.status IN ('ENROLLED', 'IN_PROGRESS') ORDER BY e.enrolledAt DESC")
    List<TrainingEnrollment> findActiveEnrollments(@Param("userId") Integer userId);
    
    @Query("SELECT AVG(e.score) FROM TrainingEnrollment e WHERE e.program.id = :programId AND e.score IS NOT NULL")
    Double getAverageScoreByProgram(@Param("programId") Integer programId);
    
    @Query("SELECT COUNT(e) FROM TrainingEnrollment e WHERE e.user.id = :userId AND e.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") Integer userId);
}

