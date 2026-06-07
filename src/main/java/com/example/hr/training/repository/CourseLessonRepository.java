package com.example.hr.training.repository;

import com.example.hr.training.entity.Course;
import com.example.hr.training.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseLessonRepository extends JpaRepository<CourseLesson, Integer> {
    
    List<CourseLesson> findByCourse(Course course);
    
    List<CourseLesson> findByCourseAndIsActiveTrueOrderByOrderIndexAsc(Course course);
    
    long countByCourse(Course course);
}

