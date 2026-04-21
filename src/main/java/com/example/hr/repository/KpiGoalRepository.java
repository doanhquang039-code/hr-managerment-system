package com.example.hr.repository;

import com.example.hr.enums.KpiStatus;
import com.example.hr.models.KpiGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KpiGoalRepository extends JpaRepository<KpiGoal, Integer> {

    List<KpiGoal> findByUserId(Integer userId);

    List<KpiGoal> findByUserIdAndStatus(Integer userId, KpiStatus status);

    List<KpiGoal> findByDepartmentId(Integer departmentId);

    List<KpiGoal> findByStatus(KpiStatus status);

    @Query("SELECT k FROM KpiGoal k WHERE k.user.id = :userId AND k.endDate >= :today AND k.status = 'ACTIVE'")
    List<KpiGoal> findActiveGoalsByUser(@Param("userId") Integer userId, @Param("today") LocalDate today);

    @Query("SELECT AVG(k.achievementPct) FROM KpiGoal k WHERE k.user.id = :userId AND k.status = 'COMPLETED'")
    Double avgAchievementByUser(@Param("userId") Integer userId);
}
