package com.example.hr.task.repository;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hr.enums.TaskType;
import com.example.hr.task.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    // Giá»¯ láº¡i hÃ m tÃ¬m theo tÃªn (Há»£p lá»‡ vÃ¬ Task cÃ³ field taskName)
    List<Task> findByTaskNameContaining(String keyword);

    List<Task> findByTaskNameContainingIgnoreCase(String keyword);

    List<Task> findByTaskType(TaskType taskType);

    @Query("SELECT t FROM Task t WHERE " +
           "(:keyword IS NULL OR LOWER(t.taskName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:taskType IS NULL OR t.taskType = :taskType) " +
           "AND (:extraShift IS NULL OR t.isExtraShift = :extraShift) " +
           "AND (:startDate IS NULL OR t.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.endDate <= :endDate)")
    org.springframework.data.domain.Page<Task> searchTasks(@Param("keyword") String keyword,
                           @Param("taskType") TaskType taskType,
                           @Param("extraShift") Boolean extraShift,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           org.springframework.data.domain.Pageable pageable);
}


