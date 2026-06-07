package com.example.hr.engagement.controller;

import com.example.hr.models.*;
import com.example.hr.engagement.entity.PulseSurvey;
import com.example.hr.engagement.entity.SocialPost;
import com.example.hr.enums.UserStatus;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.engagement.service.EmployeeEngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EngagementController {
    
    private final EmployeeEngagementService engagementService;
    private final AuthUserHelper authUserHelper;
    private final UserRepository userRepository;
    
    // ===== Social Feed =====
    
    @GetMapping({"/engagement/feed", "/user1/feed"})
    public String socialFeed(Model model) {
        model.addAttribute("posts", engagementService.getPublicPosts());
        model.addAttribute("trending", engagementService.getTrendingPosts());
        return "user1/social-feed";
    }
    
    @PostMapping({"/engagement/post/create", "/user1/post/create"})
    public String createPost(@RequestParam String content,
                            @RequestParam(required = false) String images,
                            @RequestParam(defaultValue = "POST") String type,
                            @RequestParam(defaultValue = "true") Boolean isPublic,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            engagementService.createPost(user, content, images, type, isPublic);
            ra.addFlashAttribute("success", "ÄÄƒng bÃ i thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user1/feed";
    }
    
    @PostMapping("/engagement/post/{id}/like")
    @ResponseBody
    public int likePost(@PathVariable Integer id) {
        SocialPost post = engagementService.likePost(id);
        return post.getLikeCount();
    }
    
    // ===== Recognition =====
    
    @GetMapping({"/engagement/recognition", "/user1/recognition"})
    public String recognitionWall(Model model) {
        model.addAttribute("recognitions", engagementService.getPublicRecognitions());
        return "user1/recognition-wall";
    }
    
    @GetMapping("/engagement/recognition/give")
    public String giveRecognitionForm(Model model) {
        return "engagement/give-recognition";
    }
    
    @PostMapping({"/engagement/recognition/give", "/user1/recognition/give"})
    public String giveRecognition(@RequestParam Integer recipientId,
                                 @RequestParam String type,
                                 @RequestParam String title,
                                 @RequestParam String message,
                                 @RequestParam(defaultValue = "10") Integer points,
                                 @RequestParam(defaultValue = "true") Boolean isPublic,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        try {
            User giver = authUserHelper.getCurrentUser(auth);
            // Get recipient from UserRepository
            User recipient = new User();
            recipient.setId(recipientId);
            
            engagementService.giveRecognition(recipient, giver, type, title, message, points, isPublic);
            ra.addFlashAttribute("success", "Gá»­i lá»i khen thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user1/recognition";
    }
    
    @GetMapping("/engagement/my-points")
    public String myPoints(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        
        model.addAttribute("totalPoints", engagementService.getUserTotalPoints(user));
        model.addAttribute("recognitions", engagementService.getUserRecognitions(user));
        
        return "engagement/my-points";
    }
    
    // ===== Surveys =====
    
    @GetMapping({"/engagement/surveys", "/user1/surveys"})
    public String surveys(Model model) {
        model.addAttribute("surveys", engagementService.getActiveSurveys());
        return "user1/surveys";
    }
    
    @GetMapping({"/engagement/survey/{id}", "/user1/survey/{id}"})
    public String takeSurvey(@PathVariable Integer id, Model model) {
        // Get survey details
        model.addAttribute("surveyId", id);
        return "engagement/take-survey";
    }
    
    @PostMapping({"/engagement/survey/{id}/submit", "/user1/survey/{id}/submit"})
    public String submitSurvey(@PathVariable Integer id,
                              @RequestParam String answers,
                              Authentication auth,
                              RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            PulseSurvey survey = new PulseSurvey();
            survey.setId(id);
            
            engagementService.submitResponse(survey, user, answers);
            ra.addFlashAttribute("success", "Cáº£m Æ¡n báº¡n Ä‘Ã£ tham gia kháº£o sÃ¡t!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user1/surveys";
    }
    
    // ===== Referrals =====
    
    @GetMapping({"/engagement/referrals", "/user1/referrals"})
    public String myReferrals(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        model.addAttribute("referrals", engagementService.getUserReferrals(user));
        return "user1/my-referrals";
    }
    
    // ===== ADMIN ROUTES =====
    
    @GetMapping("/admin/engagement/surveys")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminSurveyList(Model model) {
        model.addAttribute("surveys", engagementService.getAllSurveys());
        model.addAttribute("stats", engagementService.getSurveyStats());
        return "admin/survey-list";
    }
    
    @GetMapping("/admin/engagement/surveys/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminSurveyCreateForm(Model model) {
        model.addAttribute("survey", new PulseSurvey());
        return "admin/survey-form";
    }
    
    @PostMapping("/admin/engagement/surveys/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminSurveyCreate(@ModelAttribute PulseSurvey survey,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        try {
            User createdBy = authUserHelper.getCurrentUser(auth);
            engagementService.createSurvey(
                survey.getTitle(),
                survey.getDescription(),
                survey.getQuestions(),
                survey.getStartDate(),
                survey.getEndDate(),
                survey.getIsAnonymous(),
                createdBy
            );
            ra.addFlashAttribute("success", "Táº¡o kháº£o sÃ¡t thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lá»—i: " + e.getMessage());
        }
        return "redirect:/admin/engagement/surveys";
    }
    
    @GetMapping("/admin/engagement/surveys/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminSurveyEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("survey", engagementService.getSurveyById(id));
        return "admin/survey-form";
    }
    
    @PostMapping("/admin/engagement/surveys/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminSurveyUpdate(@PathVariable Integer id,
                                   @ModelAttribute PulseSurvey survey,
                                   RedirectAttributes ra) {
        try {
            engagementService.updateSurvey(id, survey);
            ra.addFlashAttribute("success", "Cáº­p nháº­t kháº£o sÃ¡t thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lá»—i: " + e.getMessage());
        }
        return "redirect:/admin/engagement/surveys";
    }
    
    @PostMapping("/admin/engagement/surveys/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminSurveyDelete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            engagementService.deleteSurvey(id);
            ra.addFlashAttribute("success", "XÃ³a kháº£o sÃ¡t thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lá»—i: " + e.getMessage());
        }
        return "redirect:/admin/engagement/surveys";
    }
    
    @GetMapping("/admin/engagement/recognition")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String adminRecognitionList(@RequestParam(required = false) String type, Model model) {
        var recognitions = engagementService.getRecognitionsByType(type);
        if (recognitions.isEmpty()) {
            var users = userRepository.findByStatus(UserStatus.ACTIVE);
            if (users.size() >= 2) {
                String firstType = type != null && !type.isBlank() ? type : "GREAT_JOB";
                engagementService.giveRecognition(users.get(0), users.get(1), firstType,
                        "HoÃ n thÃ nh tá»‘t cÃ´ng viá»‡c", "Ghi nháº­n tinh tháº§n chá»§ Ä‘á»™ng vÃ  káº¿t quáº£ tá»‘t trong tuáº§n.", 20, true);
                engagementService.giveRecognition(users.get(1), users.get(0), "TEAM_PLAYER",
                        "Há»— trá»£ Ä‘á»“ng Ä‘á»™i", "Cáº£m Æ¡n vÃ¬ Ä‘Ã£ há»— trá»£ team xá»­ lÃ½ cÃ´ng viá»‡c Ä‘Ãºng háº¡n.", 15, true);
                recognitions = engagementService.getRecognitionsByType(type);
            }
        }
        model.addAttribute("recognitions", recognitions);
        model.addAttribute("stats", engagementService.getRecognitionStats());
        model.addAttribute("selectedType", type);
        model.addAttribute("recognitionTypes", java.util.List.of("THANK_YOU", "GREAT_JOB", "TEAM_PLAYER", "INNOVATION", "LEADERSHIP"));
        return "admin/recognition-list";
    }
}


