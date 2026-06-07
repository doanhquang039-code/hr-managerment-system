package com.example.hr.controllers;

import com.example.hr.enums.ReviewStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.PerformanceReview;
import com.example.hr.models.User;
import com.example.hr.repository.PerformanceReviewRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PerformanceReviewController {

    @Autowired private PerformanceReviewRepository reviewRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthUserHelper authUserHelper;
    @Autowired private NotificationService notificationService;

    // ==================== ADMIN ====================

    @GetMapping("/admin/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminList(@RequestParam(required = false) String cycle, Model model) {
        List<PerformanceReview> reviews = (cycle != null && !cycle.isBlank())
                ? reviewRepository.findByCycle(cycle)
                : reviewRepository.findAllWithUsers();

        model.addAttribute("reviews", reviews);
        model.addAttribute("cycle", cycle);
        model.addAttribute("statuses", ReviewStatus.values());
        return "admin/review-list";
    }

    @GetMapping("/admin/reviews/add")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showAddForm(Model model) {
        model.addAttribute("review", new PerformanceReview());
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", ReviewStatus.values());
        model.addAttribute("periods", generatePeriods());
        return "admin/review-form";
    }

    @GetMapping("/admin/reviews/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showEditForm(@PathVariable Integer id, Model model) {
        PerformanceReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review khÃ´ng tá»“n táº¡i: " + id));
        model.addAttribute("review", review);
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", ReviewStatus.values());
        model.addAttribute("periods", generatePeriods());
        return "admin/review-form";
    }

    @PostMapping("/admin/reviews/save")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String save(@ModelAttribute PerformanceReview review,
                       Authentication auth,
                       RedirectAttributes redirectAttributes) {
        // GÃ¡n reviewer lÃ  ngÆ°á»i Ä‘ang Ä‘Äƒng nháº­p
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (review.getReviewer() == null) {
            review.setReviewer(currentUser);
        }
        // Set ngÃ y Ä‘Ã¡nh giÃ¡ náº¿u chÆ°a cÃ³
        if (review.getReviewDate() == null) {
            review.setReviewDate(LocalDate.now());
        }
        // TÃ­nh overall score
        review.calculateOverallScore();

        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("successMsg", "âœ… ÄÃ¡nh giÃ¡ Ä‘Ã£ Ä‘Æ°á»£c lÆ°u!");
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/reviews/approve/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String approve(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        PerformanceReview review = reviewRepository.findById(id).orElseThrow();
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("successMsg", "âœ… ÄÃ¡nh giÃ¡ Ä‘Ã£ Ä‘Æ°á»£c duyá»‡t!");
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/reviews/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        reviewRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "ðŸ—‘ï¸ ÄÃ£ xoÃ¡ Ä‘Ã¡nh giÃ¡!");
        return "redirect:/admin/reviews";
    }

    // ==================== USER VIEW ====================

    @GetMapping("/user1/reviews")
    @PreAuthorize("isAuthenticated()")
    public String userReviews(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        List<PerformanceReview> reviews = reviewRepository.findByUserOrdered(user);
        model.addAttribute("reviews", reviews);
        model.addAttribute("user", user);
        model.addAttribute("unreadNotifications", notificationService.countUnread(user));
        return "user1/reviews";
    }

    // ==================== HELPER ====================

    private List<String> generatePeriods() {
        int year = LocalDate.now().getYear();
        return List.of(
                year + "-Q1", year + "-Q2", year + "-Q3", year + "-Q4",
                (year - 1) + "-Q1", (year - 1) + "-Q2", (year - 1) + "-Q3", (year - 1) + "-Q4",
                String.valueOf(year), String.valueOf(year - 1)
        );
    }
}


