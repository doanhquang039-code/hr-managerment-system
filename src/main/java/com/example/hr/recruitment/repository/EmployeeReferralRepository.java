package com.example.hr.recruitment.repository;

import com.example.hr.recruitment.entity.EmployeeReferral;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeReferralRepository extends JpaRepository<EmployeeReferral, Integer> {
    
    List<EmployeeReferral> findByReferrer(User referrer);
    
    List<EmployeeReferral> findByJobPosting(JobPosting jobPosting);
    
    List<EmployeeReferral> findByStatus(String status);
    
    List<EmployeeReferral> findByReferrerAndStatus(User referrer, String status);
    
    long countByReferrerAndStatus(User referrer, String status);
    
    long countByStatus(String status);
}


