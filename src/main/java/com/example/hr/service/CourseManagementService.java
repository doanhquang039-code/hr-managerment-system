package com.example.hr.service;

import com.example.hr.models.*;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseManagementService {
    
    private final CourseRepository courseRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    
    // ===== Course Management =====
    
    /**
     * Create course
     */
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }
    
    /**
     * Get all active courses
     */
    @Transactional(readOnly = true)
    public List<Course> getActiveCourses() {
        return courseRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Course getActiveCourseById(Integer id) {
        return courseRepository.findById(id)
                .filter(course -> Boolean.TRUE.equals(course.getIsActive()))
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }
    
    /**
     * Get courses by category
     */
    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category);
    }
    
    /**
     * Search courses
     */
    @Transactional(readOnly = true)
    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }
    
    /**
     * Get mandatory courses
     */
    @Transactional(readOnly = true)
    public List<Course> getMandatoryCourses() {
        return courseRepository.findByIsMandatoryTrue();
    }
    
    // ===== Lesson Management =====
    
    /**
     * Add lesson to course
     */
    public CourseLesson addLesson(Course course, CourseLesson lesson) {
        lesson.setCourse(course);
        return lessonRepository.save(lesson);
    }
    
    /**
     * Get course lessons
     */
    @Transactional(readOnly = true)
    public List<CourseLesson> getCourseLessons(Course course) {
        return lessonRepository.findByCourseAndIsActiveTrueOrderByOrderIndexAsc(course);
    }
    
    // ===== Enrollment Management =====
    
    /**
     * Enroll user in course
     */
    public CourseEnrollment enrollUser(User user, Course course) {
        // Check if already enrolled
        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new RuntimeException("User already enrolled in this course");
        }
        
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus("ENROLLED");
        enrollment.setProgressPercent(0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        
        return enrollmentRepository.save(enrollment);
    }
    
    /**
     * Get user enrollments
     */
    @Transactional(readOnly = true)
    public List<CourseEnrollment> getUserEnrollments(User user) {
        return enrollmentRepository.findByUser(user);
    }
    
    /**
     * Get enrollment by user and course
     */
    @Transactional(readOnly = true)
    public Optional<CourseEnrollment> getEnrollment(User user, Course course) {
        return enrollmentRepository.findByUserAndCourse(user, course);
    }

    @Transactional(readOnly = true)
    public CourseEnrollment getEnrollmentById(Integer enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
    }
    
    /**
     * Update enrollment progress
     */
    public CourseEnrollment updateProgress(Integer enrollmentId, Integer progressPercent) {
        Optional<CourseEnrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new RuntimeException("Enrollment not found");
        }
        
        CourseEnrollment enrollment = enrollmentOpt.get();
        enrollment.setProgressPercent(progressPercent);
        
        if (progressPercent >= 100) {
            enrollment.setStatus("COMPLETED");
            enrollment.setCompletedAt(LocalDateTime.now());
        } else if (progressPercent > 0) {
            enrollment.setStatus("IN_PROGRESS");
            if (enrollment.getStartedAt() == null) {
                enrollment.setStartedAt(LocalDateTime.now());
            }
        }
        
        return enrollmentRepository.save(enrollment);
    }
    
    /**
     * Get completed courses count for user
     */
    @Transactional(readOnly = true)
    public long getCompletedCoursesCount(User user) {
        return enrollmentRepository.countCompletedByUser(user);
    }
    
    // ===== Quiz Management =====
    
    /**
     * Create quiz
     */
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }
    
    /**
     * Add question to quiz
     */
    public QuizQuestion addQuestion(Quiz quiz, QuizQuestion question) {
        question.setQuiz(quiz);
        return questionRepository.save(question);
    }
    
    /**
     * Get quiz questions
     */
    @Transactional(readOnly = true)
    public List<QuizQuestion> getQuizQuestions(Quiz quiz) {
        return questionRepository.findByQuizOrderByOrderIndexAsc(quiz);
    }
    
    /**
     * Submit quiz attempt
     */
    public QuizAttempt submitQuizAttempt(User user, Quiz quiz, String answers, Integer score, Integer totalPoints) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setAnswers(answers);
        attempt.setScore(score);
        attempt.setTotalPoints(totalPoints);
        attempt.setCompletedAt(LocalDateTime.now());
        
        // Check if passed
        if (quiz.getPassingScore() != null) {
            double percentage = (score * 100.0) / totalPoints;
            attempt.setPassed(percentage >= quiz.getPassingScore());
        }
        
        return attemptRepository.save(attempt);
    }
    
    /**
     * Get user's quiz attempts
     */
    @Transactional(readOnly = true)
    public List<QuizAttempt> getUserQuizAttempts(User user, Quiz quiz) {
        return attemptRepository.findByUserAndQuiz(user, quiz);
    }
    
    /**
     * Get best quiz score for user
     */
    @Transactional(readOnly = true)
    public Optional<QuizAttempt> getBestScore(User user, Quiz quiz) {
        return attemptRepository.findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
    }
}
