package com.example.hr.repository;

import com.example.hr.models.Shift;
import com.example.hr.models.ShiftAssignment;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Integer> {
    
    List<ShiftAssignment> findByUser(User user);
    
    List<ShiftAssignment> findByUserAndAssignedDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    Optional<ShiftAssignment> findByUserAndAssignedDate(User user, LocalDate date);
    
    List<ShiftAssignment> findByShift(Shift shift);
    
    List<ShiftAssignment> findByAssignedDate(LocalDate date);
}


