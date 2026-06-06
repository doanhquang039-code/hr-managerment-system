package com.example.hr.controllers;

import com.example.hr.models.Candidate;
import com.example.hr.enums.Role;
import com.example.hr.models.JobPosting;
import com.example.hr.models.User;
import com.example.hr.enums.UserStatus;
import com.example.hr.repository.CandidateRepository;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPostingRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.List;

@Controller
@RequestMapping("/hiring")
@PreAuthorize("hasAnyRole('ADMIN','HIRING','MANAGER')")
public class RecruitmentController {

    @Autowired private JobPostingRepository jobPostingRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private JobPositionRepository jobPositionRepository;
    @Autowired private UserRepository userRepository;

    // ==================== DASHBOARD ====================

    @GetMapping
    public String hiringRoot() {
        return "redirect:/hiring/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Job postings statistics
        List<JobPosting> postings = jobPostingRepository.findAll();
        long totalJobs = postings.size();
        long activeJobs = postings.stream().filter(p -> "OPEN".equals(p.getStatus())).count();
        
        // Candidate statistics
        long totalCandidates = candidateRepository.count();
        long newCandidates = candidateRepository.countByCurrentStage("NEW");
        long screeningCount = candidateRepository.countByCurrentStage("SCREENING");
        long interviewCount = candidateRepository.countByCurrentStage("INTERVIEW");
        long offerCount = candidateRepository.countByCurrentStage("OFFER");
        long hiredCount = candidateRepository.countByCurrentStage("HIRED");
        long rejectedCount = candidateRepository.countByCurrentStage("REJECTED");

        // Hiring Overview DTO
        HiringOverview hiringOverview = new HiringOverview();
        hiringOverview.setTotalJobs(totalJobs);
        hiringOverview.setActiveJobs(activeJobs);
        hiringOverview.setTotalCandidates(totalCandidates);
        hiringOverview.setTotalInterviews(interviewCount); // Simplified
        hiringOverview.setAvgApplicationsPerJob(activeJobs > 0 ? (double) totalCandidates / activeJobs : 0);
        hiringOverview.setAvgCandidateScore(75.0); // TODO: Calculate from actual scores

        // Candidate Pipeline
        java.util.Map<String, Long> candidatePipeline = new java.util.HashMap<>();
        candidatePipeline.put("APPLIED", newCandidates);
        candidatePipeline.put("SCREENING", screeningCount);
        candidatePipeline.put("INTERVIEW", interviewCount);
        candidatePipeline.put("OFFER", offerCount);
        candidatePipeline.put("HIRED", hiredCount);
        candidatePipeline.put("REJECTED", rejectedCount);

        // Recent job postings (last 5) – null-safe sort
        List<JobPosting> recentJobs = postings.stream()
                .filter(p -> p.getCreatedAt() != null)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        // Recent candidates (last 10) – null-safe sort
        List<Candidate> recentCandidates = candidateRepository.findAll().stream()
                .filter(c -> c.getAppliedAt() != null)
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        // Upcoming interviews - empty for now (TODO: implement Interview entity)
        List<Object> upcomingInterviews = new java.util.ArrayList<>();

        model.addAttribute("hiringOverview", hiringOverview);
        model.addAttribute("candidatePipeline", candidatePipeline);
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("recentCandidates", recentCandidates);
        model.addAttribute("upcomingInterviews", upcomingInterviews);
        
        // Legacy attributes for backward compatibility
        model.addAttribute("postings", postings);
        model.addAttribute("openCount", activeJobs);
        model.addAttribute("totalCandidates", totalCandidates);
        model.addAttribute("newCandidates", newCandidates);
        model.addAttribute("screeningCount", screeningCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("offerCount", offerCount);
        model.addAttribute("hiredCount", hiredCount);
        model.addAttribute("rejectedCount", rejectedCount);
        
        return "hiring/dashboard";
    }

    @GetMapping("/users")
    public String hiringUsers() {
        return "redirect:/admin/users?role=HIRING";
    }
    
    // Inner class for Hiring Overview DTO
    public static class HiringOverview {
        private long totalJobs;
        private long activeJobs;
        private long totalCandidates;
        private long totalInterviews;
        private double avgApplicationsPerJob;
        private double avgCandidateScore;
        
        public long getTotalJobs() { return totalJobs; }
        public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }
        public long getActiveJobs() { return activeJobs; }
        public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }
        public long getTotalCandidates() { return totalCandidates; }
        public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }
        public long getTotalInterviews() { return totalInterviews; }
        public void setTotalInterviews(long totalInterviews) { this.totalInterviews = totalInterviews; }
        public double getAvgApplicationsPerJob() { return avgApplicationsPerJob; }
        public void setAvgApplicationsPerJob(double avgApplicationsPerJob) { this.avgApplicationsPerJob = avgApplicationsPerJob; }
        public double getAvgCandidateScore() { return avgCandidateScore; }
        public void setAvgCandidateScore(double avgCandidateScore) { this.avgCandidateScore = avgCandidateScore; }
    }

    @GetMapping("/postings")
    public String listPostings(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String category,
                               Model model) {
        List<JobPosting> postings = (keyword != null && !keyword.isBlank())
                ? jobPostingRepository.findByTitleContainingIgnoreCase(keyword)
                : jobPostingRepository.findAll();
        postings = filterPostingsByCategory(postings, category);
        model.addAttribute("postings", postings);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        return "hiring/posting-list";
    }

    private List<JobPosting> filterPostingsByCategory(List<JobPosting> postings, String category) {
        if (category == null || category.isBlank()) {
            return postings;
        }
        String normalized = category.trim().toUpperCase(Locale.ROOT);
        return postings.stream()
                .filter(p -> switch (normalized) {
                    case "FULL_TIME" -> "FULL_TIME".equalsIgnoreCase(p.getEmploymentType());
                    case "PART_TIME" -> "PART_TIME".equalsIgnoreCase(p.getEmploymentType());
                    case "INTERN" -> "INTERNSHIP".equalsIgnoreCase(p.getEmploymentType())
                            || "INTERN".equalsIgnoreCase(p.getExperienceLevel());
                    case "JUNIOR" -> "ENTRY".equalsIgnoreCase(p.getExperienceLevel())
                            || "JUNIOR".equalsIgnoreCase(p.getExperienceLevel());
                    case "SECURITY" -> containsIgnoreCase(p.getTitle(), "bảo vệ")
                            || containsIgnoreCase(p.getTitle(), "bao ve")
                            || (p.getPosition() != null
                            && (containsIgnoreCase(p.getPosition().getPositionName(), "bảo vệ")
                            || containsIgnoreCase(p.getPosition().getPositionName(), "bao ve")));
                    default -> true;
                })
                .toList();
    }

    private boolean containsIgnoreCase(String value, String text) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));
    }

    @GetMapping("/postings/add")
    public String showAddPosting(Model model) {
        model.addAttribute("posting", new JobPosting());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", jobPositionRepository.findAll());
        return "hiring/posting-form";
    }

    @GetMapping("/postings/edit/{id}")
    public String showEditPosting(@PathVariable Integer id, Model model) {
        JobPosting posting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin tuyển dụng: " + id));
        model.addAttribute("posting", posting);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", jobPositionRepository.findAll());
        return "hiring/posting-form";
    }

    @PostMapping("/postings/save")
    public String savePosting(@ModelAttribute JobPosting posting, RedirectAttributes ra) {
        jobPostingRepository.save(posting);
        ra.addFlashAttribute("successMsg", "✅ Tin tuyển dụng đã được lưu!");
        return "redirect:/hiring/postings";
    }

    @GetMapping("/postings/close/{id}")
    public String closePosting(@PathVariable Integer id, RedirectAttributes ra) {
        JobPosting posting = jobPostingRepository.findById(id).orElseThrow();
        posting.setStatus("CLOSED");
        jobPostingRepository.save(posting);
        ra.addFlashAttribute("successMsg", "Đã đóng tin tuyển dụng.");
        return "redirect:/hiring/postings";
    }

    @GetMapping("/postings/delete/{id}")
    public String deletePosting(@PathVariable Integer id, RedirectAttributes ra) {
        jobPostingRepository.deleteById(id);
        ra.addFlashAttribute("successMsg", "🗑️ Đã xoá tin tuyển dụng.");
        return "redirect:/hiring/postings";
    }

    // ==================== JOBS MANAGEMENT ====================

    @GetMapping("/jobs/list")
    public String jobsList(Model model) {
        List<JobPosting> allJobs = jobPostingRepository.findAll();
        
        List<JobPosting> activeJobsList = allJobs.stream()
                .filter(j -> "OPEN".equals(j.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        List<JobPosting> draftJobsList = allJobs.stream()
                .filter(j -> "DRAFT".equals(j.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        List<JobPosting> closedJobsList = allJobs.stream()
                .filter(j -> "CLOSED".equals(j.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        long totalApplications = candidateRepository.count();

        model.addAttribute("activeJobsList", activeJobsList);
        model.addAttribute("draftJobsList", draftJobsList);
        model.addAttribute("closedJobsList", closedJobsList);
        model.addAttribute("allJobsList", allJobs);
        model.addAttribute("activeJobs", activeJobsList.size());
        model.addAttribute("draftJobs", draftJobsList.size());
        model.addAttribute("closedJobs", closedJobsList.size());
        model.addAttribute("totalApplications", totalApplications);
        return "hiring/jobs/list";
    }

    // ==================== CANDIDATES ====================
    // NOTE: Candidate management has been moved to CandidateController
    // All /hiring/candidates/* endpoints are now handled by CandidateController
}
