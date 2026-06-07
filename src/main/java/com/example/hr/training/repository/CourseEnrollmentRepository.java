package com.example.hr.training.repository;

import com.example.hr.training.entity.Course;
import com.example.hr.training.entity.CourseEnrollment;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Integer> {
    
    List<CourseEnrollment> findByUser(User user);
    
    List<CourseEnrollment> findByCourse(Course course);
    
    Optional<CourseEnrollment> findByUserAndCourse(User user, Course course);
    
    List<CourseEnrollment> findByUserAndStatus(User user, String status);
    
    boolean existsByUserAndCourse(User user, Course course);
    
    long countByStatus(String status);
    
    @Query("SELECT AVG(e.progressPercent) FROM CourseEnrollment e WHERE e.course = :course")
    Double getAverageProgress(@Param("course") Course course);
    
    @Query("SELECT COUNT(e) FROM CourseEnrollment e WHERE e.user = :user AND e.status = 'COMPLETED'")
    long countCompletedByUser(@Param("user") User user);
}

