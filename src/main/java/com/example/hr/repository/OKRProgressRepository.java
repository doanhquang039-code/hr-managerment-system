package com.example.hr.repository;

import com.example.hr.models.KeyResult;
import com.example.hr.models.OKRProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OKRProgressRepository extends JpaRepository<OKRProgress, Long> {
    
    List<OKRProgress> findByKeyResultOrderByCreatedAtDesc(KeyResult keyResult);
    
    List<OKRProgress> findByKeyResultIdOrderByCreatedAtDesc(Long keyResultId);
}


