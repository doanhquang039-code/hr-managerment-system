package com.example.hr.service;




import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.department.entity.Department;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.dto.OKRCreateDTO;
import com.example.hr.enums.OKRStatus;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OKRService {
    
    private final ObjectiveRepository objectiveRepository;
    private final KeyResultRepository keyResultRepository;
    private final OKRProgressRepository okrProgressRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    
    @Transactional
    public Objective createObjective(OKRCreateDTO dto, User createdBy) {
        Objective objective = new Objective();
        objective.setTitle(dto.getTitle());
        objective.setDescription(dto.getDescription());
        objective.setLevel(dto.getLevel());
        objective.setStatus(OKRStatus.DRAFT);
        objective.setStartDate(dto.getStartDate());
        objective.setEndDate(dto.getEndDate());
        objective.setProgress(0.0);
        
        if (dto.getOwnerId() != null) {
            User owner = userRepository.findById(dto.getOwnerId().intValue())
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            objective.setOwner(owner);
        }
        
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId().intValue())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            objective.setDepartment(department);
        }
        
        if (dto.getParentObjectiveId() != null) {
            Objective parent = objectiveRepository.findById(dto.getParentObjectiveId())
                    .orElseThrow(() -> new RuntimeException("Parent objective not found"));
            objective.setParentObjective(parent);
        }
        
        objective = objectiveRepository.save(objective);
        
        // Create key results
        if (dto.getKeyResults() != null && !dto.getKeyResults().isEmpty()) {
            for (OKRCreateDTO.KeyResultDTO krDto : dto.getKeyResults()) {
                KeyResult keyResult = new KeyResult();
                keyResult.setObjective(objective);
                keyResult.setTitle(krDto.getTitle());
                keyResult.setDescription(krDto.getDescription());
                keyResult.setMeasurementType(krDto.getMeasurementType());
                keyResult.setStartValue(BigDecimal.ZERO);
                keyResult.setTargetValue(krDto.getTargetValue() != null ? BigDecimal.valueOf(krDto.getTargetValue()) : BigDecimal.ZERO);
                keyResult.setCurrentValue(BigDecimal.ZERO);
                keyResult.setUnit(krDto.getUnit() != null ? krDto.getUnit() : "");
                keyResult.setWeight(krDto.getWeight() != null ? krDto.getWeight() : 100);
                keyResult.calculateProgress();
                
                keyResultRepository.save(keyResult);
            }
        }
        
        return objective;
    }
    
    @Transactional
    public KeyResult updateKeyResultProgress(Long keyResultId, Double newValue, String notes, User updatedBy) {
        KeyResult keyResult = keyResultRepository.findById(keyResultId)
                .orElseThrow(() -> new RuntimeException("Key result not found"));
        
        BigDecimal previousValue = keyResult.getCurrentValue();
        keyResult.setCurrentValue(BigDecimal.valueOf(newValue));
        keyResult.calculateProgress();
        keyResult = keyResultRepository.save(keyResult);
        
        // Log progress
        OKRProgress progress = new OKRProgress();
        progress.setKeyResult(keyResult);
        progress.setUpdatedBy(updatedBy);
        progress.setPreviousValue(previousValue.doubleValue()); // Convert BigDecimal to Double
        progress.setNewValue(newValue);
        progress.setNotes(notes);
        okrProgressRepository.save(progress);
        
        // Update objective progress
        Objective objective = keyResult.getObjective();
        objective.calculateProgress();
        objectiveRepository.save(objective);
        
        return keyResult;
    }
    
    @Transactional
    public Objective updateObjectiveStatus(Long objectiveId, OKRStatus newStatus) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found"));
        
        objective.setStatus(newStatus);
        return objectiveRepository.save(objective);
    }
    
    @Transactional(readOnly = true)
    public List<Objective> getUserObjectives(User user) {
        return objectiveRepository.findAccessibleObjectives(user);
    }
    
    @Transactional(readOnly = true)
    public List<Objective> getDepartmentObjectives(Department department) {
        return objectiveRepository.findByDepartmentOrderByCreatedAtDesc(department);
    }
    
    @Transactional(readOnly = true)
    public List<Objective> getCompanyObjectives() {
        return objectiveRepository.findByLevelAndStatusOrderByCreatedAtDesc("COMPANY", OKRStatus.ACTIVE);
    }
    
    @Transactional(readOnly = true)
    public List<Objective> getOverdueObjectives() {
        return objectiveRepository.findOverdueObjectives(LocalDate.now(), OKRStatus.ACTIVE);
    }
    
    @Transactional(readOnly = true)
    public List<OKRProgress> getKeyResultHistory(Long keyResultId) {
        return okrProgressRepository.findByKeyResultIdOrderByCreatedAtDesc(keyResultId);
    }
    
    @Transactional
    public void deleteObjective(Long objectiveId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found"));
        
        // Check if has child objectives
        List<Objective> children = objectiveRepository.findByParentObjective(objective);
        if (!children.isEmpty()) {
            throw new RuntimeException("Cannot delete objective with child objectives");
        }
        
        objectiveRepository.delete(objective);
    }
    
    @Transactional
    public Objective updateObjective(Long objectiveId, OKRCreateDTO dto) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found"));
        
        objective.setTitle(dto.getTitle());
        objective.setDescription(dto.getDescription());
        objective.setStartDate(dto.getStartDate());
        objective.setEndDate(dto.getEndDate());
        
        return objectiveRepository.save(objective);
    }
}


