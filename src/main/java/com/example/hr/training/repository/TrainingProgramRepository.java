package com.example.hr.training.repository;

import com.example.hr.enums.TrainingStatus;
import com.example.hr.training.entity.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Integer>,
        JpaSpecificationExecutor<TrainingProgram> {

    List<TrainingProgram> findByStatus(TrainingStatus status);

    List<TrainingProgram> findByDepartmentId(Integer departmentId);

    @Query("SELECT t FROM TrainingProgram t WHERE t.status IN ('PLANNED', 'IN_PROGRESS') ORDER BY t.startDate")
    List<TrainingProgram> findActivePrograms();

    @Query("SELECT t FROM TrainingProgram t WHERE t.startDate > :today " +
           "AND t.status = 'PLANNED' ORDER BY t.startDate")
    List<TrainingProgram> findUpcomingPrograms(@Param("today") LocalDate today);

    @Query("SELECT t FROM TrainingProgram t WHERE t.startDate <= :today " +
           "AND t.endDate >= :today AND t.status = 'IN_PROGRESS'")
    List<TrainingProgram> findOngoingPrograms(@Param("today") LocalDate today);

    @Query("SELECT t FROM TrainingProgram t WHERE t.startDate BETWEEN :start AND :end")
    List<TrainingProgram> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.budget), 0) FROM TrainingProgram t WHERE YEAR(t.startDate) = :year")
    java.math.BigDecimal sumBudgetByYear(@Param("year") int year);

    @Query("SELECT t.trainingType, COUNT(t) FROM TrainingProgram t GROUP BY t.trainingType")
    List<Object[]> countByTrainingType();

    @Query("SELECT t.status, COUNT(t) FROM TrainingProgram t GROUP BY t.status")
    List<Object[]> countByStatus();

    long countByStatus(TrainingStatus status);
}

