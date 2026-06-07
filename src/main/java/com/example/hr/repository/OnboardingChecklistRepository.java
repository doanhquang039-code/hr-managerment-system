package com.example.hr.repository;

import com.example.hr.models.OnboardingChecklist;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingChecklistRepository extends JpaRepository<OnboardingChecklist, Integer> {

    @Query("SELECT o FROM OnboardingChecklist o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.assignedTo ORDER BY o.createdAt DESC")
    List<OnboardingChecklist> findAllWithUsers();

    @Query("SELECT o FROM OnboardingChecklist o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.assignedTo WHERE o.isCompleted = :completed ORDER BY o.createdAt DESC")
    List<OnboardingChecklist> findByIsCompletedWithUsers(@Param("completed") boolean completed);

    @Query("SELECT o FROM OnboardingChecklist o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.assignedTo WHERE o.category = :category ORDER BY o.createdAt DESC")
    List<OnboardingChecklist> findByCategoryWithUsers(@Param("category") String category);
    
    List<OnboardingChecklist> findByUser(User user);
    
    List<OnboardingChecklist> findByUserAndIsCompleted(User user, boolean isCompleted);
    
    List<OnboardingChecklist> findByAssignedTo(User assignedTo);
    
    List<OnboardingChecklist> findByCategory(String category);
    
    long countByIsCompleted(boolean isCompleted);
    
    @Query("SELECT COUNT(o) * 100.0 / (SELECT COUNT(o2) FROM OnboardingChecklist o2 WHERE o2.user = :user) FROM OnboardingChecklist o WHERE o.user = :user AND o.isCompleted = true")
    Double getCompletionPercentage(@Param("user") User user);
    
    long countByUserAndIsCompleted(User user, boolean isCompleted);
}


