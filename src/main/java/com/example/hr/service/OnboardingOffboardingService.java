package com.example.hr.service;

import com.example.hr.models.*;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingOffboardingService {
    
    private final OnboardingChecklistRepository checklistRepository;
    private final ExitInterviewRepository exitInterviewRepository;
    
    // ===== Onboarding =====
    
    /**
     * Create onboarding checklist item
     */
    public OnboardingChecklist createChecklistItem(User user, String taskName, String description,
                                                   Integer dayOffset, String category, User assignedTo) {
        OnboardingChecklist item = new OnboardingChecklist();
        item.setUser(user);
        item.setTaskName(taskName);
        item.setDescription(description);
        item.setDayOffset(dayOffset);
        item.setCategory(category);
        item.setAssignedTo(assignedTo);
        item.setIsCompleted(false);
        
        return checklistRepository.save(item);
    }
    
    /**
     * Get user's onboarding checklist
     */
    @Transactional(readOnly = true)
    public List<OnboardingChecklist> getUserChecklist(User user) {
        return checklistRepository.findByUser(user);
    }
    
    /**
     * Get pending checklist items
     */
    @Transactional(readOnly = true)
    public List<OnboardingChecklist> getPendingItems(User user) {
        return checklistRepository.findByUserAndIsCompleted(user, false);
    }
    
    /**
     * Complete checklist item
     */
    public OnboardingChecklist completeItem(Integer itemId) {
        Optional<OnboardingChecklist> itemOpt = checklistRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("Checklist item not found");
        }
        
        OnboardingChecklist item = itemOpt.get();
        item.setIsCompleted(true);
        item.setCompletedAt(LocalDateTime.now());
        
        return checklistRepository.save(item);
    }
    
    /**
     * Get completion percentage
     */
    @Transactional(readOnly = true)
    public Double getCompletionPercentage(User user) {
        Double percentage = checklistRepository.getCompletionPercentage(user);
        return percentage != null ? percentage : 0.0;
    }
    
    /**
     * Create standard onboarding checklist for new employee
     */
    public void createStandardOnboardingChecklist(User newEmployee, User hr) {
        // Day 0 - First day
        createChecklistItem(newEmployee, "Complete paperwork", 
            "Fill out all required employment forms", 0, "PAPERWORK", hr);
        createChecklistItem(newEmployee, "Receive company equipment", 
            "Get laptop, phone, and access cards", 0, "IT_SETUP", hr);
        createChecklistItem(newEmployee, "Office tour", 
            "Tour of office facilities and introduction to team", 0, "INTRODUCTION", hr);
        
        // Day 1-7 - First week
        createChecklistItem(newEmployee, "IT account setup", 
            "Email, Slack, and system access", 1, "IT_SETUP", hr);
        createChecklistItem(newEmployee, "Company orientation", 
            "Learn about company culture, values, and policies", 2, "TRAINING", hr);
        createChecklistItem(newEmployee, "Meet team members", 
            "One-on-one meetings with team", 3, "INTRODUCTION", hr);
        
        // Day 30 - First month
        createChecklistItem(newEmployee, "30-day check-in", 
            "Review progress and address any concerns", 30, "OTHER", hr);
        
        // Day 90 - Probation end
        createChecklistItem(newEmployee, "90-day performance review", 
            "Formal review and feedback session", 90, "OTHER", hr);
    }
    
    // ===== Exit Interview =====
    
    /**
     * Create exit interview
     */
    public ExitInterview createExitInterview(User user, LocalDate interviewDate, User interviewer) {
        ExitInterview interview = new ExitInterview();
        interview.setUser(user);
        interview.setInterviewDate(interviewDate);
        interview.setInterviewer(interviewer);
        
        return exitInterviewRepository.save(interview);
    }
    
    /**
     * Update exit interview
     */
    public ExitInterview updateExitInterview(Integer interviewId, String reasonForLeaving,
                                            Integer satisfactionRating, String feedback,
                                            String suggestions, Boolean wouldRecommend,
                                            Boolean wouldRehire, String responses) {
        Optional<ExitInterview> interviewOpt = exitInterviewRepository.findById(interviewId);
        if (interviewOpt.isEmpty()) {
            throw new RuntimeException("Exit interview not found");
        }
        
        ExitInterview interview = interviewOpt.get();
        interview.setReasonForLeaving(reasonForLeaving);
        interview.setSatisfactionRating(satisfactionRating);
        interview.setFeedback(feedback);
        interview.setSuggestions(suggestions);
        interview.setWouldRecommend(wouldRecommend);
        interview.setWouldRehire(wouldRehire);
        interview.setResponses(responses);
        
        return exitInterviewRepository.save(interview);
    }
    
    /**
     * Get exit interview for user
     */
    @Transactional(readOnly = true)
    public Optional<ExitInterview> getExitInterview(User user) {
        return exitInterviewRepository.findByUser(user);
    }
    
    /**
     * Get all exit interviews
     */
    @Transactional(readOnly = true)
    public List<ExitInterview> getAllExitInterviews() {
        return exitInterviewRepository.findAll();
    }
    
    /**
     * Get average satisfaction rating
     */
    @Transactional(readOnly = true)
    public Double getAverageSatisfactionRating() {
        Double avg = exitInterviewRepository.getAverageSatisfactionRating();
        return avg != null ? avg : 0.0;
    }
    
    /**
     * Get recommendation rate
     */
    @Transactional(readOnly = true)
    public Double getRecommendationRate() {
        long total = exitInterviewRepository.count();
        if (total == 0) return 0.0;
        
        long wouldRecommend = exitInterviewRepository.countByWouldRecommend(true);
        return (wouldRecommend * 100.0) / total;
    }
    
    // ===== Admin Methods =====
    
    /**
     * Get all checklists
     */
    @Transactional(readOnly = true)
    public List<OnboardingChecklist> getAllChecklists() {
        return checklistRepository.findAllWithUsers();
    }

    @Transactional(readOnly = true)
    public List<OnboardingChecklist> getChecklistsByStatus(String status) {
        if ("completed".equalsIgnoreCase(status)) {
            return checklistRepository.findByIsCompletedWithUsers(true);
        }
        if ("pending".equalsIgnoreCase(status)) {
            return checklistRepository.findByIsCompletedWithUsers(false);
        }
        return getAllChecklists();
    }

    @Transactional(readOnly = true)
    public List<OnboardingChecklist> getChecklistsByCategory(String category) {
        if (category == null || category.isBlank()) {
            return getAllChecklists();
        }
        return checklistRepository.findByCategoryWithUsers(category);
    }
    
    /**
     * Get checklist statistics
     */
    @Transactional(readOnly = true)
    public ChecklistStats getChecklistStats() {
        long totalItems = checklistRepository.count();
        long completedItems = checklistRepository.countByIsCompleted(true);
        long pendingItems = checklistRepository.countByIsCompleted(false);
        
        double completionRate = totalItems > 0 ? 
            (double) completedItems / totalItems * 100 : 0;
        
        return new ChecklistStats(totalItems, completedItems, pendingItems,
            Math.round(completionRate * 10.0) / 10.0);
    }
    
    // Inner class for stats
    public static class ChecklistStats {
        public long totalItems;
        public long completedItems;
        public long pendingItems;
        public double completionRate;
        
        public ChecklistStats(long totalItems, long completedItems, 
                             long pendingItems, double completionRate) {
            this.totalItems = totalItems;
            this.completedItems = completedItems;
            this.pendingItems = pendingItems;
            this.completionRate = completionRate;
        }
    }
}
