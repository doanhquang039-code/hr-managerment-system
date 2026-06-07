package com.example.hr.repository;

import com.example.hr.models.BenefitEnrollment;
import com.example.hr.models.BenefitPlan;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BenefitEnrollmentRepository extends JpaRepository<BenefitEnrollment, Integer> {
    
    List<BenefitEnrollment> findByUser(User user);
    
    List<BenefitEnrollment> findByBenefitPlan(BenefitPlan benefitPlan);
    
    List<BenefitEnrollment> findByUserAndStatus(User user, String status);
    
    Optional<BenefitEnrollment> findByUserAndBenefitPlanAndStatus(User user, BenefitPlan benefitPlan, String status);
    
    boolean existsByUserAndBenefitPlanAndStatus(User user, BenefitPlan benefitPlan, String status);
    
    long countByBenefitPlanAndStatus(BenefitPlan benefitPlan, String status);
}


