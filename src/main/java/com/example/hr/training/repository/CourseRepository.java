package com.example.hr.training.repository;

import com.example.hr.training.entity.Course;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    
    List<Course> findByIsActiveTrue();
    
    List<Course> findByCategory(String category);
    
    List<Course> findByLevel(String level);
    
    List<Course> findByInstructor(User instructor);
    
    List<Course> findByIsMandatoryTrue();
    
    List<Course> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    
    @Query("SELECT c FROM Course c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Course> findRecentCourses();
    
    long countByIsActiveTrue();
}

