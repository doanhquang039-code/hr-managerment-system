package com.example.hr.controllers;

import com.example.hr.enums.NotificationType;
import com.example.hr.models.User;
import com.example.hr.recruitment.entity.Candidate;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.recruitment.repository.CandidateRepository;
import com.example.hr.recruitment.repository.JobPostingRepository;
import com.example.hr.repository.NotificationRepository;
import com.example.hr.service.NotificationPushService;
import com.example.hr.service.NotificationService;
import com.example.hr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PublicCareersController – Trang tuyển dụng công khai.
 * Không yêu cầu đăng nhập. Ứng viên bên ngoài có thể xem job và nộp CV.
 */
@Controller
@RequestMapping("/careers")
@RequiredArgsConstructor
@Slf4j
public class PublicCareersController {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NotificationPushService notificationPushService;

    /**
     * GET /careers – Trang chủ tuyển dụng: giới thiệu công ty + danh sách job
     */
    @GetMapping({"", "/"})
    public String careersHome(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            Model model) {

        List<JobPosting> jobs = jobPostingRepository.findActiveJobPostings(LocalDate.now());

        // Lọc theo keyword (title/description)
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            jobs = jobs.stream()
                .filter(j -> (j.getTitle() != null && j.getTitle().toLowerCase().contains(kw))
                          || (j.getDescription() != null && j.getDescription().toLowerCase().contains(kw))
                          || (j.getLocation() != null && j.getLocation().toLowerCase().contains(kw)))
                .toList();
        }
        // Lọc theo loại công việc
        if (type != null && !type.isBlank()) {
            jobs = jobs.stream()
                .filter(j -> type.equalsIgnoreCase(j.getEmploymentType()))
                .toList();
        }
        // Lọc theo cấp độ kinh nghiệm
        if (level != null && !level.isBlank()) {
            jobs = jobs.stream()
                .filter(j -> level.equalsIgnoreCase(j.getExperienceLevel()))
                .toList();
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedLevel", level);
        return "public/careers";
    }

    /**
     * GET /careers/job/{id} – Chi tiết vị trí tuyển dụng
     */
    @GetMapping("/job/{id}")
    public String jobDetail(@PathVariable Integer id, Model model) {
        JobPosting job = jobPostingRepository.findById(id).orElse(null);
        if (job == null || !job.isActive()) {
            return "redirect:/careers";
        }
        // Tăng view count
        job.setViewsCount((job.getViewsCount() == null ? 0 : job.getViewsCount()) + 1);
        jobPostingRepository.save(job);

        model.addAttribute("job", job);
        return "public/careers-detail";
    }

    /**
     * POST /careers/apply – Ứng viên nộp CV (không cần đăng nhập)
     */
    @PostMapping("/apply")
    public String applyForJob(
            @RequestParam Integer jobId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String coverLetter,
            @RequestParam(required = false) String linkedinUrl,
            @RequestParam(required = false) Integer yearsOfExperience,
            @RequestParam(required = false) Integer expectedSalary,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) MultipartFile cvFile,
            RedirectAttributes redirectAttributes) {

        JobPosting job = jobPostingRepository.findById(jobId).orElse(null);
        if (job == null || !job.isActive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vị trí tuyển dụng không còn hoạt động.");
            return "redirect:/careers";
        }

        // Kiểm tra email đã apply chưa (cùng vị trí)
        List<Candidate> existing = candidateRepository.findByJobPostingOrderByAppliedAtDesc(job);
        boolean alreadyApplied = existing.stream().anyMatch(c -> email.equalsIgnoreCase(c.getEmail()));
        if (alreadyApplied) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Email " + email + " đã nộp CV cho vị trí này rồi!");
            return "redirect:/careers/job/" + jobId;
        }

        // Upload CV file nếu có
        String resumeUrl = null;
        if (cvFile != null && !cvFile.isEmpty()) {
            resumeUrl = saveUploadedFile(cvFile);
        }

        // Tạo Candidate record
        Candidate candidate = new Candidate();
        candidate.setFullName(fullName);
        candidate.setEmail(email);
        candidate.setPhoneNumber(phoneNumber);
        candidate.setCoverLetter(coverLetter);
        candidate.setLinkedinUrl(linkedinUrl);
        candidate.setYearsOfExperience(yearsOfExperience);
        candidate.setExpectedSalary(expectedSalary);
        candidate.setSkills(skills);
        candidate.setResumeUrl(resumeUrl);
        candidate.setJobPosting(job);
        candidate.setCurrentStage("APPLIED");
        candidate.setSource("WEBSITE");
        candidate.setAppliedAt(LocalDateTime.now());
        candidateRepository.save(candidate);

        // Tăng application count của job
        job.setApplicationsCount((job.getApplicationsCount() == null ? 0 : job.getApplicationsCount()) + 1);
        jobPostingRepository.save(job);

        // Tạo notification trong DB cho tất cả HIRING + ADMIN và push real-time
        String notifMsg = "📄 CV mới từ " + fullName + " cho vị trí: " + job.getTitle();
        String notifLink = "/hiring/candidates";
        notifyHiringAndAdmin(notifMsg, notifLink);

        log.info("New CV application: {} ({}) for job '{}'", fullName, email, job.getTitle());

        redirectAttributes.addFlashAttribute("successMessage",
            "🎉 Cảm ơn " + fullName + "! CV của bạn đã được gửi thành công. Chúng tôi sẽ liên hệ sớm qua email " + email);
        return "redirect:/careers?applied=true";
    }

    // ---- Private helpers ----

    /**
     * Lưu file CV upload lên thư mục uploads/cv/
     */
    private String saveUploadedFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/cv/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/" + uploadDir + fileName;
        } catch (IOException e) {
            log.error("Failed to save CV file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tạo DB notification + push WebSocket real-time cho tất cả HIRING & ADMIN
     */
    private void notifyHiringAndAdmin(String message, String link) {
        try {
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                String roleName = u.getEffectiveRoleName();
                if ("HIRING".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
                    notificationService.createNotification(u, message, NotificationType.INFO, link);
                }
            }
            // Ngoài ra push WebSocket riêng để đảm bảo tất cả online user nhận ngay
            notificationPushService.pushToHiringAndAdmins(message, link);
        } catch (Exception e) {
            log.error("Failed to notify hiring/admin: {}", e.getMessage());
        }
    }
}
