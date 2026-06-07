package com.example.hr.repository;

import com.example.hr.models.Quiz;
import com.example.hr.models.QuizAttempt;
import com.example.hr.models.User;
import com.example.hr.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
    
    List<QuizAttempt> findByUser(User user);
    
    List<QuizAttempt> findByQuiz(Quiz quiz);
    
    List<QuizAttempt> findByUserAndQuiz(User user, Quiz quiz);
    
    Optional<QuizAttempt> findFirstByUserAndQuizOrderByScoreDesc(User user, Quiz quiz);
    
    @Query("SELECT AVG(a.score) FROM QuizAttempt a WHERE a.quiz = :quiz")
    Double getAverageScore(@Param("quiz") Quiz quiz);

    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.user WHERE a.quiz = :quiz AND a.completedAt IS NOT NULL ORDER BY a.score DESC, a.completedAt ASC")
    List<QuizAttempt> findLeaderboardByQuiz(@Param("quiz") Quiz quiz);

    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.user WHERE a.quiz = :quiz AND a.user.role = :role AND a.completedAt IS NOT NULL ORDER BY a.score DESC, a.completedAt ASC")
    List<QuizAttempt> findLeaderboardByQuizAndRole(@Param("quiz") Quiz quiz, @Param("role") Role role);
    
    long countByUserAndPassed(User user, boolean passed);
}


