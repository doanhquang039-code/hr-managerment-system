package com.example.hr.engagement.api;

import com.example.hr.models.*;
import com.example.hr.engagement.entity.PulseSurvey;
import com.example.hr.engagement.entity.Recognition;
import com.example.hr.engagement.entity.SocialPost;
import com.example.hr.engagement.entity.SurveyResponse;
import com.example.hr.recruitment.entity.EmployeeReferral;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.engagement.service.EmployeeEngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/engagement")
@RequiredArgsConstructor
public class EngagementApiController {
    
    private final EmployeeEngagementService engagementService;
    private final AuthUserHelper authUserHelper;
    
    // ===== Social Feed =====
    
    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPosts() {
        List<SocialPost> posts = engagementService.getPublicPosts();
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping("/post/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> request,
                                       Authentication auth) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            String content = request.get("content").toString();
            String images = request.getOrDefault("images", "").toString();
            String type = request.getOrDefault("type", "GENERAL").toString();
            Boolean isPublic = Boolean.parseBoolean(request.getOrDefault("isPublic", "true").toString());
            
            SocialPost post = engagementService.createPost(user, content, images, type, isPublic);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("post", post);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/post/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> likePost(@PathVariable Integer postId, Authentication auth) {
        try {
            engagementService.likePost(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Liked successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/posts/trending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTrendingPosts() {
        List<SocialPost> posts = engagementService.getTrendingPosts();
        return ResponseEntity.ok(posts);
    }
    
    // ===== Recognition =====
    
    @PostMapping("/recognition/give")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> giveRecognition(@RequestBody Map<String, Object> request,
                                            Authentication auth) {
        try {
            User giver = authUserHelper.getCurrentUser(auth);
            
            Integer recipientId = Integer.parseInt(request.get("recipientId").toString());
            String type = request.get("type").toString();
            String title = request.get("title").toString();
            String message = request.get("message").toString();
            Integer points = Integer.parseInt(request.getOrDefault("points", "50").toString());
            Boolean isPublic = Boolean.parseBoolean(request.getOrDefault("isPublic", "true").toString());
            
            User recipient = new User();
            recipient.setId(recipientId);
            
            Recognition recognition = engagementService.giveRecognition(recipient, giver, type, title, message, points, isPublic);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vinh danh Ä‘Ã£ Ä‘Æ°á»£c gá»­i");
            response.put("recognition", recognition);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/recognition/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecentRecognitions() {
        List<Recognition> recognitions = engagementService.getPublicRecognitions();
        return ResponseEntity.ok(recognitions);
    }
    
    @GetMapping("/recognition/my-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyPoints(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        Integer totalPoints = engagementService.getUserTotalPoints(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalPoints", totalPoints);
        
        return ResponseEntity.ok(response);
    }
    
    // ===== Surveys =====
    
    @GetMapping("/surveys/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getActiveSurveys() {
        List<PulseSurvey> surveys = engagementService.getActiveSurveys();
        return ResponseEntity.ok(surveys);
    }
    
    @PostMapping("/survey/{surveyId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitSurvey(@PathVariable Integer surveyId,
                                         @RequestBody Map<String, Object> request,
                                         Authentication auth) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            
            PulseSurvey survey = new PulseSurvey();
            survey.setId(surveyId);
            
            String answers = request.get("answers").toString();
            
            SurveyResponse response = engagementService.submitResponse(survey, user, answers);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cáº£m Æ¡n báº¡n Ä‘Ã£ tham gia kháº£o sÃ¡t");
            result.put("response", response);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // ===== Referrals =====
    
    @PostMapping("/referral/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitReferral(@RequestBody Map<String, Object> request,
                                           Authentication auth) {
        try {
            User referrer = authUserHelper.getCurrentUser(auth);
            
            String candidateName = request.get("candidateName").toString();
            String candidateEmail = request.get("candidateEmail").toString();
            String candidatePhone = request.get("candidatePhone").toString();
            Integer jobPostingId = Integer.parseInt(request.get("jobPostingId").toString());
            String resumeUrl = request.getOrDefault("resumeUrl", "").toString();
            String notes = request.getOrDefault("notes", "").toString();
            
            JobPosting jobPosting = new JobPosting();
            jobPosting.setId(jobPostingId);
            
            EmployeeReferral referral = engagementService.submitReferral(
                referrer, jobPosting, candidateName, candidateEmail, candidatePhone, resumeUrl, notes
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Giá»›i thiá»‡u á»©ng viÃªn thÃ nh cÃ´ng");
            response.put("referral", referral);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/referral/my-referrals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyReferrals(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        List<EmployeeReferral> referrals = engagementService.getUserReferrals(user);
        
        return ResponseEntity.ok(referrals);
    }
}


