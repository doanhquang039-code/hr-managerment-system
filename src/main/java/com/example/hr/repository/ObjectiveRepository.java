package com.example.hr.repository;

import com.example.hr.enums.OKRStatus;
import com.example.hr.department.entity.Department;
import com.example.hr.models.Objective;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, Long> {
    
    List<Objective> findByOwnerOrderByCreatedAtDesc(User owner);
    
    List<Objective> findByDepartmentOrderByCreatedAtDesc(Department department);
    
    List<Objective> findByLevelAndStatusOrderByCreatedAtDesc(String level, OKRStatus status);
    
    List<Objective> findByStatusOrderByCreatedAtDesc(OKRStatus status);
    
    List<Objective> findByParentObjective(Objective parentObjective);
    
    @Query("SELECT o FROM Objective o WHERE o.endDate < :today AND o.status = :status")
    List<Objective> findOverdueObjectives(@Param("today") LocalDate today, @Param("status") OKRStatus status);
    
    @Query("SELECT o FROM Objective o WHERE o.owner = :user OR o.department IN " +
           "(SELECT u.department FROM User u WHERE u = :user)")
    List<Objective> findAccessibleObjectives(@Param("user") User user);
}


