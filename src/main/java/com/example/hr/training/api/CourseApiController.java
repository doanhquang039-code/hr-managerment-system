package com.example.hr.training.api;

import com.example.hr.models.*;
import com.example.hr.training.entity.*;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.training.service.CourseManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms")
@RequiredArgsConstructor
public class CourseApiController {
    
    private final CourseManagementService courseService;
    private final AuthUserHelper authUserHelper;
    
    @GetMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCourses(@RequestParam(required = false) String category,
                                       @RequestParam(required = false) String search) {
        List<Course> courses;
        
        if (search != null && !search.isBlank()) {
            courses = courseService.searchCourses(search);
        } else if (category != null && !category.isBlank()) {
            courses = courseService.getCoursesByCategory(category);
        } else {
            courses = courseService.getActiveCourses();
        }
        
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/course/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCourseDetail(@PathVariable Integer id) {
        Course course = courseService.getActiveCourses().stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<CourseLesson> lessons = courseService.getCourseLessons(course);
        
        Map<String, Object> response = new HashMap<>();
        response.put("course", course);
        response.put("lessons", lessons);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/enroll/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enrollCourse(@PathVariable Integer courseId, Authentication auth) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            Course course = courseService.getActiveCourses().stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course not found"));
            
            CourseEnrollment enrollment = courseService.enrollUser(user, course);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Enrolled successfully");
            response.put("enrollment", enrollment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/my-courses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyCourses(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        List<CourseEnrollment> enrollments = courseService.getUserEnrollments(user);
        long completedCount = courseService.getCompletedCoursesCount(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("enrollments", enrollments);
        response.put("completedCount", completedCount);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/enrollment/{enrollmentId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProgress(@PathVariable Integer enrollmentId,
                                           @RequestParam Integer progressPercent) {
        try {
            CourseEnrollment enrollment = courseService.updateProgress(enrollmentId, progressPercent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enrollment", enrollment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/quiz/{quizId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitQuiz(@PathVariable Integer quizId,
                                       @RequestBody Map<String, Object> submission,
                                       Authentication auth) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            
            // Get quiz (simplified - in real app, fetch from DB)
            Quiz quiz = new Quiz();
            quiz.setId(quizId);
            
            String answers = submission.get("answers").toString();
            Integer score = (Integer) submission.get("score");
            Integer totalPoints = (Integer) submission.get("totalPoints");
            
            QuizAttempt attempt = courseService.submitQuizAttempt(user, quiz, answers, score, totalPoints);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("attempt", attempt);
            response.put("passed", attempt.getPassed());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

