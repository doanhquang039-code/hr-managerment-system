package com.example.hr.engagement.service;

import com.example.hr.models.*;
import com.example.hr.engagement.entity.PulseSurvey;
import com.example.hr.engagement.entity.Recognition;
import com.example.hr.engagement.entity.SocialPost;
import com.example.hr.engagement.entity.SurveyResponse;
import com.example.hr.engagement.repository.PulseSurveyRepository;
import com.example.hr.engagement.repository.RecognitionRepository;
import com.example.hr.engagement.repository.SocialPostRepository;
import com.example.hr.engagement.repository.SurveyResponseRepository;
import com.example.hr.repository.*;
import com.example.hr.recruitment.entity.EmployeeReferral;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.recruitment.repository.EmployeeReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeEngagementService {
    
    private final PulseSurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final RecognitionRepository recognitionRepository;
    private final EmployeeReferralRepository referralRepository;
    private final SocialPostRepository socialPostRepository;
    
    // ===== Pulse Surveys =====
    
    /**
     * Create pulse survey
     */
    public PulseSurvey createSurvey(String title, String description, String questions,
                                   LocalDate startDate, LocalDate endDate, Boolean isAnonymous, User createdBy) {
        PulseSurvey survey = new PulseSurvey();
        survey.setTitle(title);
        survey.setDescription(description);
        survey.setQuestions(questions);
        survey.setStartDate(startDate);
        survey.setEndDate(endDate);
        survey.setIsAnonymous(isAnonymous);
        survey.setCreatedBy(createdBy);
        survey.setIsActive(true);
        
        return surveyRepository.save(survey);
    }
    
    /**
     * Get active surveys
     */
    @Transactional(readOnly = true)
    public List<PulseSurvey> getActiveSurveys() {
        return surveyRepository.findActiveSurveys(LocalDate.now());
    }
    
    /**
     * Submit survey response
     */
    public SurveyResponse submitResponse(PulseSurvey survey, User user, String answers) {
        // Check if already responded
        if (!survey.getIsAnonymous() && responseRepository.existsByUserAndSurvey(user, survey)) {
            throw new RuntimeException("You have already responded to this survey");
        }
        
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setUser(survey.getIsAnonymous() ? null : user);
        response.setAnswers(answers);
        
        return responseRepository.save(response);
    }
    
    /**
     * Get survey responses
     */
    @Transactional(readOnly = true)
    public List<SurveyResponse> getSurveyResponses(PulseSurvey survey) {
        return responseRepository.findBySurvey(survey);
    }
    
    /**
     * Get response count
     */
    @Transactional(readOnly = true)
    public long getResponseCount(PulseSurvey survey) {
        return responseRepository.countBySurvey(survey);
    }
    
    // ===== Recognition & Rewards =====
    
    /**
     * Give recognition
     */
    public Recognition giveRecognition(User recipient, User giver, String type, 
                                      String title, String message, Integer points, Boolean isPublic) {
        Recognition recognition = new Recognition();
        recognition.setRecipient(recipient);
        recognition.setGiver(giver);
        recognition.setType(type);
        recognition.setTitle(title);
        recognition.setMessage(message);
        recognition.setPoints(points);
        recognition.setIsPublic(isPublic);
        
        return recognitionRepository.save(recognition);
    }
    
    /**
     * Get public recognitions
     */
    @Transactional(readOnly = true)
    public List<Recognition> getPublicRecognitions() {
        return recognitionRepository.findRecentPublicRecognitions();
    }
    
    /**
     * Get user's recognitions
     */
    @Transactional(readOnly = true)
    public List<Recognition> getUserRecognitions(User user) {
        return recognitionRepository.findByRecipient(user);
    }
    
    /**
     * Get total points for user
     */
    @Transactional(readOnly = true)
    public Integer getUserTotalPoints(User user) {
        Integer points = recognitionRepository.getTotalPointsByRecipient(user);
        return points != null ? points : 0;
    }
    
    // ===== Employee Referrals =====
    
    /**
     * Submit referral
     */
    public EmployeeReferral submitReferral(User referrer, JobPosting jobPosting,
                                          String candidateName, String candidateEmail, 
                                          String candidatePhone, String resumeUrl, String notes) {
        EmployeeReferral referral = new EmployeeReferral();
        referral.setReferrer(referrer);
        referral.setJobPosting(jobPosting);
        referral.setCandidateName(candidateName);
        referral.setCandidateEmail(candidateEmail);
        referral.setCandidatePhone(candidatePhone);
        referral.setResumeUrl(resumeUrl);
        referral.setNotes(notes);
        referral.setStatus("SUBMITTED");
        
        return referralRepository.save(referral);
    }
    
    /**
     * Get user's referrals
     */
    @Transactional(readOnly = true)
    public List<EmployeeReferral> getUserReferrals(User user) {
        return referralRepository.findByReferrer(user);
    }
    
    /**
     * Update referral status
     */
    public EmployeeReferral updateReferralStatus(Integer referralId, String status) {
        Optional<EmployeeReferral> referralOpt = referralRepository.findById(referralId);
        if (referralOpt.isEmpty()) {
            throw new RuntimeException("Referral not found");
        }
        
        EmployeeReferral referral = referralOpt.get();
        referral.setStatus(status);
        
        return referralRepository.save(referral);
    }
    
    // ===== Social Feed =====
    
    /**
     * Create post
     */
    public SocialPost createPost(User user, String content, String images, String type, Boolean isPublic) {
        SocialPost post = new SocialPost();
        post.setUser(user);
        post.setContent(content);
        post.setImages(images);
        post.setType(type);
        post.setIsPublic(isPublic);
        post.setLikeCount(0);
        post.setCommentCount(0);
        
        return socialPostRepository.save(post);
    }
    
    /**
     * Get public posts
     */
    @Transactional(readOnly = true)
    public List<SocialPost> getPublicPosts() {
        return socialPostRepository.findPublicPostsOrderByCreatedAtDesc();
    }
    
    /**
     * Get trending posts
     */
    @Transactional(readOnly = true)
    public List<SocialPost> getTrendingPosts() {
        return socialPostRepository.findTrendingPosts();
    }
    
    /**
     * Like post
     */
    public SocialPost likePost(Integer postId) {
        Optional<SocialPost> postOpt = socialPostRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new RuntimeException("Post not found");
        }
        
        SocialPost post = postOpt.get();
        post.setLikeCount(post.getLikeCount() + 1);
        
        return socialPostRepository.save(post);
    }
    
    // ===== Admin Methods =====
    
    /**
     * Get all surveys
     */
    @Transactional(readOnly = true)
    public List<PulseSurvey> getAllSurveys() {
        return surveyRepository.findAll();
    }
    
    /**
     * Get survey by ID
     */
    @Transactional(readOnly = true)
    public PulseSurvey getSurveyById(Integer id) {
        return surveyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Survey not found"));
    }
    
    /**
     * Update survey
     */
    public PulseSurvey updateSurvey(Integer id, PulseSurvey updatedSurvey) {
        PulseSurvey survey = getSurveyById(id);
        survey.setTitle(updatedSurvey.getTitle());
        survey.setDescription(updatedSurvey.getDescription());
        survey.setQuestions(updatedSurvey.getQuestions());
        survey.setStartDate(updatedSurvey.getStartDate());
        survey.setEndDate(updatedSurvey.getEndDate());
        survey.setIsAnonymous(updatedSurvey.getIsAnonymous());
        survey.setIsActive(updatedSurvey.getIsActive());
        return surveyRepository.save(survey);
    }
    
    /**
     * Delete survey
     */
    public void deleteSurvey(Integer id) {
        surveyRepository.deleteById(id);
    }
    
    /**
     * Get survey statistics
     */
    @Transactional(readOnly = true)
    public SurveyStats getSurveyStats() {
        long totalSurveys = surveyRepository.count();
        long activeSurveys = surveyRepository.countByIsActiveTrue();
        long totalResponses = responseRepository.count();
        
        double participationRate = totalSurveys > 0 ? 
            (double) totalResponses / totalSurveys * 100 : 0;
        
        return new SurveyStats(totalSurveys, activeSurveys, totalResponses, 
            Math.round(participationRate * 10.0) / 10.0);
    }
    
    /**
     * Get all recognitions
     */
    @Transactional(readOnly = true)
    public List<Recognition> getAllRecognitions() {
        return recognitionRepository.findAllWithUsers();
    }

    public List<Recognition> getRecognitionsByType(String type) {
        if (type == null || type.isBlank()) {
            return getAllRecognitions();
        }
        return recognitionRepository.findByTypeWithUsers(type);
    }
    
    /**
     * Get recognition statistics
     */
    @Transactional(readOnly = true)
    public RecognitionStats getRecognitionStats() {
        long totalRecognitions = recognitionRepository.count();
        long thisMonth = recognitionRepository.countThisMonth();
        Integer totalPoints = recognitionRepository.getTotalPoints();
        
        return new RecognitionStats(totalRecognitions, thisMonth, 
            totalPoints != null ? totalPoints : 0);
    }
    
    // Inner classes for stats
    public static class SurveyStats {
        public long totalSurveys;
        public long activeSurveys;
        public long totalResponses;
        public double participationRate;
        
        public SurveyStats(long totalSurveys, long activeSurveys, 
                          long totalResponses, double participationRate) {
            this.totalSurveys = totalSurveys;
            this.activeSurveys = activeSurveys;
            this.totalResponses = totalResponses;
            this.participationRate = participationRate;
        }
    }
    
    public static class RecognitionStats {
        public long totalRecognitions;
        public long thisMonth;
        public int totalPoints;
        
        public RecognitionStats(long totalRecognitions, long thisMonth, int totalPoints) {
            this.totalRecognitions = totalRecognitions;
            this.thisMonth = thisMonth;
            this.totalPoints = totalPoints;
        }
    }
}


