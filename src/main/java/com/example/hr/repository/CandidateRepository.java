package com.example.hr.repository;

import com.example.hr.models.Candidate;
import com.example.hr.models.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Integer> {
    List<Candidate> findByJobPosting(JobPosting jobPosting);
    List<Candidate> findByStatus(String status);
    List<Candidate> findByFullNameContainingIgnoreCase(String name);
    long countByJobPosting(JobPosting jobPosting);
    long countByStatus(String status);
}
