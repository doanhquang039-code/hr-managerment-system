package com.example.hr.repository;

import com.example.hr.models.Quiz;
import com.example.hr.models.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer> {
    
    List<QuizQuestion> findByQuizOrderByOrderIndexAsc(Quiz quiz);
    
    long countByQuiz(Quiz quiz);
}


