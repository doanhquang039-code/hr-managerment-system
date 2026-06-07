package com.example.hr.recruitment.service;

import com.example.hr.recruitment.entity.JobPosition;
import com.example.hr.recruitment.repository.JobPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;

    /**
     * Get all active positions
     */
    @Transactional(readOnly = true)
    public List<JobPosition> getAllPositions() {
        return jobPositionRepository.findByActiveTrue();
    }

    /**
     * Get all positions (including inactive)
     */
    @Transactional(readOnly = true)
    public List<JobPosition> getAllPositionsIncludingInactive() {
        return jobPositionRepository.findAll();
    }

    /**
     * Get position by ID
     */
    @Transactional(readOnly = true)
    public JobPosition getPositionById(Integer id) {
        return jobPositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + id));
    }

    /**
     * Create new position
     */
    public JobPosition createPosition(JobPosition position) {
        position.setCreatedAt(LocalDateTime.now());
        position.setActive(true);
        return jobPositionRepository.save(position);
    }

    /**
     * Update position
     */
    public JobPosition updatePosition(Integer id, JobPosition positionData) {
        JobPosition position = getPositionById(id);
        
        position.setPositionName(positionData.getPositionName());
        position.setJobLevel(positionData.getJobLevel());
        position.setAllowanceCoeff(positionData.getAllowanceCoeff());
        position.setNextPosition(positionData.getNextPosition());
        position.setUpdatedAt(LocalDateTime.now());
        
        return jobPositionRepository.save(position);
    }

    /**
     * Delete position (soft delete)
     */
    public void deletePosition(Integer id) {
        JobPosition position = getPositionById(id);
        position.setActive(false);
        position.setUpdatedAt(LocalDateTime.now());
        jobPositionRepository.save(position);
    }

    /**
     * Search positions by name
     */
    @Transactional(readOnly = true)
    public List<JobPosition> searchPositions(String keyword) {
        return jobPositionRepository.findByPositionNameContainingIgnoreCase(keyword);
    }

    /**
     * Get positions by active status
     */
    @Transactional(readOnly = true)
    public List<JobPosition> getPositionsByActive(Boolean active) {
        return jobPositionRepository.findByActive(active);
    }
}

