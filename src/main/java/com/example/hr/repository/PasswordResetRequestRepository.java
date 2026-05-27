package com.example.hr.repository;

import com.example.hr.models.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Integer> {
    List<PasswordResetRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
