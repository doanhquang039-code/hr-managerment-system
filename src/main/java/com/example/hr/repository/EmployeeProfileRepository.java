package com.example.hr.repository;

import com.example.hr.models.EmployeeProfile;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Integer> {
    
    Optional<EmployeeProfile> findByUser(User user);
    
    boolean existsByUser(User user);
}


