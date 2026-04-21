package com.example.hr.service;

import com.example.hr.enums.ShiftAssignmentStatus;
import com.example.hr.models.ShiftAssignment;
import com.example.hr.models.WorkShift;
import com.example.hr.repository.ShiftAssignmentRepository;
import com.example.hr.repository.WorkShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public WorkShiftService(WorkShiftRepository workShiftRepository,
                            ShiftAssignmentRepository shiftAssignmentRepository) {
        this.workShiftRepository = workShiftRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    // ---- WorkShift ----

    public List<WorkShift> findAllShifts() {
        return workShiftRepository.findAll();
    }

    public List<WorkShift> findActiveShifts() {
        return workShiftRepository.findByIsActiveTrue();
    }

    public Optional<WorkShift> findShiftById(Integer id) {
        return workShiftRepository.findById(id);
    }

    public WorkShift saveShift(WorkShift shift) {
        if (shift.getId() == null) {
            shift.setCreatedAt(LocalDateTime.now());
        }
        shift.setUpdatedAt(LocalDateTime.now());
        return workShiftRepository.save(shift);
    }

    public void deleteShift(Integer id) {
        workShiftRepository.deleteById(id);
    }

    // ---- ShiftAssignment ----

    public List<ShiftAssignment> findAllAssignments() {
        return shiftAssignmentRepository.findAll();
    }

    public List<ShiftAssignment> findAssignmentsByUser(Integer userId) {
        return shiftAssignmentRepository.findByUserId(userId);
    }

    public List<ShiftAssignment> findAssignmentsByUserAndDateRange(Integer userId, LocalDate from, LocalDate to) {
        return shiftAssignmentRepository.findByUserIdAndWorkDateBetween(userId, from, to);
    }

    public List<ShiftAssignment> findAssignmentsByDateRange(LocalDate from, LocalDate to) {
        return shiftAssignmentRepository.findByWorkDateBetween(from, to);
    }

    public Optional<ShiftAssignment> findAssignmentByUserAndDate(Integer userId, LocalDate date) {
        return shiftAssignmentRepository.findByUserIdAndWorkDate(userId, date);
    }

    public ShiftAssignment saveAssignment(ShiftAssignment assignment) {
        if (assignment.getId() == null) {
            assignment.setCreatedAt(LocalDateTime.now());
        }
        assignment.setUpdatedAt(LocalDateTime.now());
        return shiftAssignmentRepository.save(assignment);
    }

    public void deleteAssignment(Integer id) {
        shiftAssignmentRepository.deleteById(id);
    }

    public Optional<ShiftAssignment> findAssignmentById(Integer id) {
        return shiftAssignmentRepository.findById(id);
    }

    public long countAssignmentsByStatus(Integer userId, LocalDate from, LocalDate to, ShiftAssignmentStatus status) {
        return shiftAssignmentRepository.countByUserIdAndWorkDateBetweenAndStatus(userId, from, to, status);
    }
}
