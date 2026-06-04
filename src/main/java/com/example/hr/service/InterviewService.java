package com.example.hr.service;

import com.example.hr.models.Interview;
import com.example.hr.models.Candidate;
import com.example.hr.models.User;
import com.example.hr.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateService candidateService;

    /**
     * Schedule new interview
     */
    @Transactional
    public Interview scheduleInterview(Interview interview) {
        interview.setCreatedAt(LocalDateTime.now());
        interview.setStatus("SCHEDULED");
        
        // Move candidate to interview stage if not already there
        if (interview.getCandidate() != null) {
            String currentStage = interview.getCandidate().getCurrentStage();
            if ("APPLIED".equals(currentStage) || "SCREENING".equals(currentStage)) {
                candidateService.moveToStage(interview.getCandidate().getId(), "INTERVIEW");
            }
        }
        
        return interviewRepository.save(interview);
    }

    /**
     * Update interview
     */
    @Transactional
    public Interview updateInterview(Integer id, Interview interviewData) {
        Interview interview = getInterviewById(id);
        
        interview.setInterviewType(interviewData.getInterviewType());
        interview.setScheduledTime(interviewData.getScheduledTime());
        interview.setDurationMinutes(interviewData.getDurationMinutes());
        interview.setLocation(interviewData.getLocation());
        interview.setMeetingLink(interviewData.getMeetingLink());
        interview.setAgenda(interviewData.getAgenda());
        interview.setUpdatedAt(LocalDateTime.now());
        
        return interviewRepository.save(interview);
    }

    /**
     * Complete interview with feedback
     */
    @Transactional
    public Interview completeInterview(Integer id, String feedback, Integer technicalScore, 
                                       Integer communicationScore, Integer culturalFitScore, 
                                       String recommendation) {
        Interview interview = getInterviewById(id);
        
        interview.setFeedback(feedback);
        interview.setTechnicalScore(technicalScore);
        interview.setCommunicationScore(communicationScore);
        interview.setCulturalFitScore(culturalFitScore);
        interview.setRecommendation(recommendation);
        interview.setStatus("COMPLETED");
        interview.setUpdatedAt(LocalDateTime.now());
        
        // Calculate overall score
        interview.calculateOverallScore();
        
        // Update candidate score
        if (interview.getCandidate() != null && interview.getOverallScore() != null) {
            candidateService.updateScore(interview.getCandidate().getId(), interview.getOverallScore());
        }
        
        return interviewRepository.save(interview);
    }

    /**
     * Cancel interview
     */
    @Transactional
    public Interview cancelInterview(Integer id, String reason) {
        Interview interview = getInterviewById(id);
        interview.setStatus("CANCELLED");
        interview.setNotes(reason);
        interview.setUpdatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }

    /**
     * Mark as no-show
     */
    @Transactional
    public Interview markNoShow(Integer id) {
        Interview interview = getInterviewById(id);
        interview.setStatus("NO_SHOW");
        interview.setUpdatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }

    /**
     * Get interview by ID
     */
    public Interview getInterviewById(Integer id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found with id: " + id));
    }

    /**
     * Get all interviews
     */
    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    /**
     * Get interviews by candidate
     */
    public List<Interview> getInterviewsByCandidate(Candidate candidate) {
        return interviewRepository.findByCandidateOrderByScheduledTimeDesc(candidate);
    }

    /**
     * Get interviews by interviewer
     */
    public List<Interview> getInterviewsByInterviewer(User interviewer) {
        return interviewRepository.findByInterviewerOrderByScheduledTimeDesc(interviewer);
    }

    /**
     * Get upcoming interviews
     */
    public List<Interview> getUpcomingInterviews(User interviewer) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusWeeks(2);
        return interviewRepository.findUpcomingInterviews(interviewer, now, futureDate);
    }

    /**
     * Get interviews by status
     */
    public List<Interview> getInterviewsByStatus(String status) {
        return interviewRepository.findByStatusOrderByScheduledTimeDesc(status);
    }

    /**
     * Get interviews by date range
     */
    public List<Interview> getInterviewsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return interviewRepository.findInterviewsByDateRange(startDate, endDate);
    }

    public Page<Interview> searchInterviews(String keyword, String status, String type,
                                            LocalDateTime startDate, LocalDateTime endDate,
                                            Pageable pageable) {
        return interviewRepository.searchInterviews(keyword, status, type, startDate, endDate, pageable);
    }

    /**
     * Get interview statistics
     */
    public InterviewStatistics getInterviewStatistics() {
        long totalInterviews = interviewRepository.count();
        long scheduledInterviews = interviewRepository.countByStatus("SCHEDULED");
        long completedInterviews = interviewRepository.countByStatus("COMPLETED");
        long cancelledInterviews = interviewRepository.countByStatus("CANCELLED");
        
        Double avgScore = interviewRepository.getAverageInterviewScore();
        
        // Get recommendation stats
        Map<String, Long> recommendationStats = new HashMap<>();
        List<Object[]> recData = interviewRepository.getRecommendationStats();
        for (Object[] row : recData) {
            recommendationStats.put((String) row[0], (Long) row[1]);
        }
        
        // Get interview type stats
        Map<String, Long> typeStats = new HashMap<>();
        List<Object[]> typeData = interviewRepository.getInterviewTypeStats();
        for (Object[] row : typeData) {
            typeStats.put((String) row[0], (Long) row[1]);
        }
        
        return new InterviewStatistics(
                totalInterviews,
                scheduledInterviews,
                completedInterviews,
                cancelledInterviews,
                avgScore != null ? avgScore : 0.0,
                recommendationStats,
                typeStats
        );
    }

    public record InterviewStatistics(
            long totalInterviews,
            long scheduledInterviews,
            long completedInterviews,
            long cancelledInterviews,
            double averageScore,
            Map<String, Long> recommendationStats,
            Map<String, Long> typeStats
    ) {}
}
