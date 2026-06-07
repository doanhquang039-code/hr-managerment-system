package com.example.hr.repository;

import com.example.hr.models.BenefitPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenefitPlanRepository extends JpaRepository<BenefitPlan, Integer> {
    
    List<BenefitPlan> findByIsActiveTrue();
    
    List<BenefitPlan> findByCategory(String category);
    
    List<BenefitPlan> findByNameContainingIgnoreCase(String name);
}


