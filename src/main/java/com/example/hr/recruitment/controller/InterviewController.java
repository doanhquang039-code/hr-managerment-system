package com.example.hr.recruitment.controller;

import com.example.hr.recruitment.entity.Interview;
import com.example.hr.recruitment.service.InterviewService;
import com.example.hr.recruitment.service.CandidateService;
import com.example.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping({"/hiring/interviews", "/admin/interviews"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
public class InterviewController {

    private final InterviewService interviewService;
    private final CandidateService candidateService;
    private final UserService userService;

    @GetMapping
    public String listInterviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        String normalizedSortBy = normalizeInterviewSort(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, normalizedSortBy));
        var interviewPage = interviewService.searchInterviews(blankToNull(search), blankToNull(status), blankToNull(type),
                startDate, endDate, pageable);
        
        var statistics = interviewService.getInterviewStatistics();
        
        model.addAttribute("interviewPage", interviewPage);
        model.addAttribute("interviews", interviewPage.getContent());
        model.addAttribute("currentPage", interviewPage.getNumber());
        model.addAttribute("totalPages", interviewPage.getTotalPages());
        model.addAttribute("totalItems", interviewPage.getTotalElements());
        model.addAttribute("sortField", normalizedSortBy);
        model.addAttribute("sortDir", direction.name().toLowerCase());
        model.addAttribute("reverseSortDir", direction == Sort.Direction.ASC ? "desc" : "asc");
        model.addAttribute("statistics", statistics);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "hiring/interviews/list";
    }

    @GetMapping("/create")
    public String createInterviewForm(@RequestParam(required = false) Integer candidateId, Model model) {
        var interview = new Interview();
        if (candidateId != null) {
            var candidate = candidateService.getCandidateById(candidateId);
            interview.setCandidate(candidate);
        }
        
        model.addAttribute("interview", interview);
        model.addAttribute("candidates", candidateService.getCandidatesByStage("INTERVIEW"));
        model.addAttribute("interviewers", userService.getUsersByRole("MANAGER"));
        
        return "hiring/interviews/create";
    }

    @PostMapping("/create")
    public String createInterview(@ModelAttribute Interview interview, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            interviewService.scheduleInterview(interview);
            redirectAttributes.addFlashAttribute("successMessage", "Interview scheduled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error scheduling interview: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/interviews";
    }

    @GetMapping("/{id}")
    public String viewInterview(@PathVariable Integer id, Model model) {
        var interview = interviewService.getInterviewById(id);
        model.addAttribute("interview", interview);
        return "hiring/interviews/view";
    }

    @GetMapping("/{id}/edit")
    public String editInterviewForm(@PathVariable Integer id, Model model) {
        var interview = interviewService.getInterviewById(id);
        model.addAttribute("interview", interview);
        model.addAttribute("candidates", candidateService.getAllCandidates());
        model.addAttribute("interviewers", userService.getUsersByRole("MANAGER"));
        return "hiring/interviews/edit";
    }

    @PostMapping("/{id}/edit")
    public String editInterview(@PathVariable Integer id, @ModelAttribute Interview interview, 
                                RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            interviewService.updateInterview(id, interview);
            redirectAttributes.addFlashAttribute("successMessage", "Interview updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating interview: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/interviews/" + id;
    }

    @GetMapping("/{id}/feedback")
    public String feedbackForm(@PathVariable Integer id, Model model) {
        var interview = interviewService.getInterviewById(id);
        model.addAttribute("interview", interview);
        return "hiring/interviews/feedback";
    }

    @PostMapping("/{id}/feedback")
    public String submitFeedback(@PathVariable Integer id,
                                 @RequestParam String feedback,
                                 @RequestParam Integer technicalScore,
                                 @RequestParam Integer communicationScore,
                                 @RequestParam Integer culturalFitScore,
                                 @RequestParam String recommendation,
                                 RedirectAttributes redirectAttributes,
                                 jakarta.servlet.http.HttpServletRequest request) {
        try {
            interviewService.completeInterview(id, feedback, technicalScore, 
                    communicationScore, culturalFitScore, recommendation);
            redirectAttributes.addFlashAttribute("successMessage", "Interview feedback submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting feedback: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/interviews/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelInterview(@PathVariable Integer id, @RequestParam String reason, 
                                  RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            interviewService.cancelInterview(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Interview cancelled!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling interview: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/interviews/" + id;
    }

    @PostMapping("/{id}/no-show")
    public String markNoShow(@PathVariable Integer id, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            interviewService.markNoShow(id);
            redirectAttributes.addFlashAttribute("successMessage", "Interview marked as no-show!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking no-show: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/interviews/" + id;
    }

    @GetMapping("/upcoming")
    public String upcomingInterviews(Model model) {
        // This would need current user context - simplified for now
        var upcomingInterviews = interviewService.getInterviewsByDateRange(
                LocalDateTime.now(), 
                LocalDateTime.now().plusWeeks(2)
        );
        
        model.addAttribute("interviews", upcomingInterviews);
        return "hiring/interviews/upcoming";
    }

    private String normalizeInterviewSort(String sortBy) {
        return switch (sortBy) {
            case "scheduledTime", "status", "interviewType", "interviewRound", "durationMinutes",
                 "overallScore", "createdAt", "updatedAt" -> sortBy;
            default -> "scheduledTime";
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}


