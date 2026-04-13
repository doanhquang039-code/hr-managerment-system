package com.example.hr.repository;

import com.example.hr.models.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Integer> {
    List<JobPosting> findByStatus(String status);
    List<JobPosting> findByTitleContainingIgnoreCase(String title);
}
