package com.example.hr.repository;

import com.example.hr.models.TeamGoal;
import com.example.hr.department.entity.Department;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeamGoalRepository extends JpaRepository<TeamGoal, Integer> {

    List<TeamGoal> findByDepartmentOrderByCreatedAtDesc(Department department);

    List<TeamGoal> findByManagerOrderByCreatedAtDesc(User manager);

    List<TeamGoal> findByStatusOrderByCreatedAtDesc(String status);

    List<TeamGoal> findByGoalTypeOrderByCreatedAtDesc(String goalType);

    @Query("SELECT g FROM TeamGoal g WHERE g.department = :department AND g.status = :status")
    List<TeamGoal> findByDepartmentAndStatus(@Param("department") Department department, @Param("status") String status);

    @Query("SELECT g FROM TeamGoal g WHERE g.manager = :manager AND g.endDate >= :currentDate AND g.status != 'COMPLETED'")
    List<TeamGoal> findActiveGoalsByManager(@Param("manager") User manager, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(g) FROM TeamGoal g WHERE g.department = :department AND g.status = 'COMPLETED'")
    long countCompletedGoalsByDepartment(@Param("department") Department department);

    @Query("SELECT AVG(g.progressPercentage) FROM TeamGoal g WHERE g.department = :department AND g.status = 'IN_PROGRESS'")
    Double getAverageProgressByDepartment(@Param("department") Department department);
    
    // Additional method for Team Analytics
    List<TeamGoal> findByDepartment(Department department);
}


