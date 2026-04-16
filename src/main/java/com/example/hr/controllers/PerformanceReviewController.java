package com.example.hr.controllers;

import com.example.hr.enums.ReviewStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.PerformanceReview;
import com.example.hr.models.User;
import com.example.hr.repository.PerformanceReviewRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String adminList(@RequestParam(required = false) String period, Model model) {
        List<PerformanceReview> reviews = (period != null && !period.isBlank())
                ? reviewRepository.findByPeriod(period)
                : reviewRepository.findAllWithUsers();

        model.addAttribute("reviews", reviews);
        model.addAttribute("period", period);
        model.addAttribute("statuses", ReviewStatus.values());
        return "admin/review-list";
    }

    @GetMapping("/admin/reviews/add")
    public String showAddForm(Model model) {
        model.addAttribute("review", new PerformanceReview());
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", ReviewStatus.values());
        model.addAttribute("periods", generatePeriods());
        return "admin/review-form";
    }

    @GetMapping("/admin/reviews/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        PerformanceReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review không tồn tại: " + id));
        model.addAttribute("review", review);
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("statuses", ReviewStatus.values());
        model.addAttribute("periods", generatePeriods());
        return "admin/review-form";
    }

    @PostMapping("/admin/reviews/save")
    public String save(@ModelAttribute PerformanceReview review,
                       Authentication auth,
                       RedirectAttributes redirectAttributes) {
        // Gán reviewer là người đang đăng nhập
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (review.getReviewer() == null) {
            review.setReviewer(currentUser);
        }
        // Set ngày đánh giá nếu chưa có
        if (review.getReviewDate() == null) {
            review.setReviewDate(LocalDate.now());
        }
        // Tính overall score
        review.calculateOverallScore();

        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("successMsg", "✅ Đánh giá đã được lưu!");
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/reviews/approve/{id}")
    public String approve(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        PerformanceReview review = reviewRepository.findById(id).orElseThrow();
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("successMsg", "✅ Đánh giá đã được duyệt!");
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/reviews/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        reviewRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMsg", "🗑️ Đã xoá đánh giá!");
        return "redirect:/admin/reviews";
    }

    // ==================== USER VIEW ====================

    @GetMapping("/user1/reviews")
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
