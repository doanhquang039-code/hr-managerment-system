package com.example.hr.repository;

import com.example.hr.enums.ShiftType;
import com.example.hr.models.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Integer> {
    List<WorkShift> findByIsActiveTrue();
    List<WorkShift> findByShiftType(ShiftType shiftType);
    Optional<WorkShift> findByShiftCode(String shiftCode);
    boolean existsByShiftCode(String shiftCode);
}
