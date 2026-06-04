package com.example.hr.service;

import com.example.hr.models.Candidate;
import com.example.hr.models.JobPosting;
import com.example.hr.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final JobPostingService jobPostingService;

    /**
     * Create new candidate application
     */
    @Transactional
    public Candidate createCandidate(Candidate candidate) {
        candidate.setAppliedAt(LocalDateTime.now());
        candidate.setCurrentStage("APPLIED");
        
        // Increment job posting application count
        if (candidate.getJobPosting() != null) {
            jobPostingService.incrementApplicationCount(candidate.getJobPosting().getId());
        }
        
        return candidateRepository.save(candidate);
    }

    /**
     * Update candidate
     */
    @Transactional
    public Candidate updateCandidate(Integer id, Candidate candidateData) {
        Candidate candidate = getCandidateById(id);
        
        candidate.setFullName(candidateData.getFullName());
        candidate.setEmail(candidateData.getEmail());
        candidate.setPhoneNumber(candidateData.getPhoneNumber());
        candidate.setResumeUrl(candidateData.getResumeUrl());
        candidate.setCoverLetter(candidateData.getCoverLetter());
        candidate.setLinkedinUrl(candidateData.getLinkedinUrl());
        candidate.setPortfolioUrl(candidateData.getPortfolioUrl());
        candidate.setYearsOfExperience(candidateData.getYearsOfExperience());
        candidate.setCurrentCompany(candidateData.getCurrentCompany());
        candidate.setCurrentPosition(candidateData.getCurrentPosition());
        candidate.setExpectedSalary(candidateData.getExpectedSalary());
        candidate.setAvailabilityDate(candidateData.getAvailabilityDate());
        candidate.setSkills(candidateData.getSkills());
        candidate.setEducation(candidateData.getEducation());
        candidate.setUpdatedAt(LocalDateTime.now());
        
        return candidateRepository.save(candidate);
    }

    /**
     * Move candidate to next stage
     */
    @Transactional
    public Candidate moveToStage(Integer id, String stage) {
        Candidate candidate = getCandidateById(id);
        candidate.setCurrentStage(stage);
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    /**
     * Update candidate score
     */
    @Transactional
    public Candidate updateScore(Integer id, Integer score) {
        Candidate candidate = getCandidateById(id);
        candidate.setOverallScore(score);
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    /**
     * Add notes to candidate
     */
    @Transactional
    public Candidate addNotes(Integer id, String notes) {
        Candidate candidate = getCandidateById(id);
        String existingNotes = candidate.getNotes();
        String newNotes = existingNotes != null 
                ? existingNotes + "\n\n" + LocalDateTime.now() + ": " + notes
                : LocalDateTime.now() + ": " + notes;
        candidate.setNotes(newNotes);
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    /**
     * Hire candidate
     */
    @Transactional
    public Candidate hireCandidate(Integer id) {
        Candidate candidate = getCandidateById(id);
        candidate.setCurrentStage("HIRED");
        String workflowNote = LocalDateTime.now() + ": HIRED - ready for onboarding checklist, contract setup and LMS assignment";
        candidate.setNotes(candidate.getNotes() != null && !candidate.getNotes().isBlank()
                ? candidate.getNotes() + "\n\n" + workflowNote
                : workflowNote);
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    /**
     * Reject candidate
     */
    @Transactional
    public Candidate rejectCandidate(Integer id, String reason) {
        Candidate candidate = getCandidateById(id);
        candidate.setCurrentStage("REJECTED");
        candidate.setUpdatedAt(LocalDateTime.now());
        
        // Add rejection reason to notes
        addNotes(id, "REJECTED: " + reason);
        
        return candidateRepository.save(candidate);
    }

    /**
     * Get candidate by ID
     */
    public Candidate getCandidateById(Integer id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));
    }

    /**
     * Get all candidates
     */
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    /**
     * Get candidates by job posting
     */
    public List<Candidate> getCandidatesByJobPosting(JobPosting jobPosting) {
        return candidateRepository.findByJobPostingOrderByAppliedAtDesc(jobPosting);
    }

    /**
     * Get candidates by stage
     */
    public List<Candidate> getCandidatesByStage(String stage) {
        return candidateRepository.findByCurrentStageOrderByAppliedAtDesc(stage);
    }

    /**
     * Search candidates
     */
    public org.springframework.data.domain.Page<Candidate> searchCandidates(String keyword, String stage, org.springframework.data.domain.Pageable pageable) {
        return candidateRepository.searchCandidates(keyword, stage, pageable);
    }

    /**
     * Delete candidate
     */
    @Transactional
    public void deleteCandidate(Integer id) {
        candidateRepository.deleteById(id);
    }

    /**
     * Get top candidates by score
     */
    public List<Candidate> getTopCandidates(Integer minScore) {
        return candidateRepository.findByMinScore(minScore);
    }

    /**
     * Get candidate statistics
     */
    public CandidateStatistics getCandidateStatistics() {
        long totalCandidates = candidateRepository.count();
        
        // Get candidates by stage
        Map<String, Long> candidatesByStage = new HashMap<>();
        List<Object[]> stageData = candidateRepository.getCandidatesByStage();
        for (Object[] row : stageData) {
            candidatesByStage.put((String) row[0], (Long) row[1]);
        }
        
        // Get candidates by source
        Map<String, Long> candidatesBySource = new HashMap<>();
        List<Object[]> sourceData = candidateRepository.getCandidatesBySource();
        for (Object[] row : sourceData) {
            candidatesBySource.put((String) row[0], (Long) row[1]);
        }
        
        Double avgScore = candidateRepository.getAverageScore();
        
        return new CandidateStatistics(
                totalCandidates,
                candidatesByStage,
                candidatesBySource,
                avgScore != null ? avgScore : 0.0
        );
    }

    public record CandidateStatistics(
            long totalCandidates,
            Map<String, Long> candidatesByStage,
            Map<String, Long> candidatesBySource,
            double averageScore
    ) {}
}
