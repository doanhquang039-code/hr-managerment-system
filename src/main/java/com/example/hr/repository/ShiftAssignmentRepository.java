package com.example.hr.repository;

import com.example.hr.enums.ShiftAssignmentStatus;
import com.example.hr.models.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Integer> {

    List<ShiftAssignment> findByUserId(Integer userId);

    List<ShiftAssignment> findByUserIdAndWorkDateBetween(Integer userId, LocalDate from, LocalDate to);

    List<ShiftAssignment> findByWorkDateBetween(LocalDate from, LocalDate to);

    Optional<ShiftAssignment> findByUserIdAndWorkDate(Integer userId, LocalDate workDate);

    List<ShiftAssignment> findByStatus(ShiftAssignmentStatus status);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shift.id = :shiftId AND sa.workDate = :date")
    List<ShiftAssignment> findByShiftIdAndWorkDate(@Param("shiftId") Integer shiftId, @Param("date") LocalDate date);

    long countByUserIdAndWorkDateBetweenAndStatus(Integer userId, LocalDate from, LocalDate to, ShiftAssignmentStatus status);
}
