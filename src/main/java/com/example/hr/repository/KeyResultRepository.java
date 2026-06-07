package com.example.hr.repository;

import com.example.hr.models.KeyResult;
import com.example.hr.models.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyResultRepository extends JpaRepository<KeyResult, Long> {
    
    List<KeyResult> findByObjectiveOrderByCreatedAtAsc(Objective objective);
    
    List<KeyResult> findByObjectiveIdOrderByCreatedAtAsc(Long objectiveId);
}


