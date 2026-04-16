package com.example.hr.controllers;

import com.example.hr.models.Candidate;
import com.example.hr.models.JobPosting;
import com.example.hr.models.User;
import com.example.hr.enums.UserStatus;
import com.example.hr.repository.CandidateRepository;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPostingRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hiring")
public class RecruitmentController {

    @Autowired private JobPostingRepository jobPostingRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private JobPositionRepository jobPositionRepository;
    @Autowired private UserRepository userRepository;

    // ==================== JOB POSTINGS ====================

    @GetMapping
    public String dashboard(Model model) {
        List<JobPosting> postings = jobPostingRepository.findAll();
        long openCount = postings.stream().filter(p -> "OPEN".equals(p.getStatus())).count();
        long totalCandidates = candidateRepository.count();
        long newCandidates      = candidateRepository.countByStatus("NEW");
        long screeningCount     = candidateRepository.countByStatus("SCREENING");
        long interviewCount     = candidateRepository.countByStatus("INTERVIEW");
        long offerCount         = candidateRepository.countByStatus("OFFER");
        long hiredCount         = candidateRepository.countByStatus("HIRED");
        long rejectedCount      = candidateRepository.countByStatus("REJECTED");

        model.addAttribute("postings", postings);
        model.addAttribute("openCount", openCount);
        model.addAttribute("totalCandidates", totalCandidates);
        model.addAttribute("newCandidates", newCandidates);
        model.addAttribute("screeningCount", screeningCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("offerCount", offerCount);
        model.addAttribute("hiredCount", hiredCount);
        model.addAttribute("rejectedCount", rejectedCount);
        return "hiring/dashboard";
    }

    @GetMapping("/postings")
    public String listPostings(@RequestParam(required = false) String keyword, Model model) {
        List<JobPosting> postings = (keyword != null && !keyword.isBlank())
                ? jobPostingRepository.findByTitleContainingIgnoreCase(keyword)
                : jobPostingRepository.findAll();
        model.addAttribute("postings", postings);
        model.addAttribute("keyword", keyword);
        return "hiring/posting-list";
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

    // ==================== CANDIDATES ====================

    @GetMapping("/candidates")
    public String listCandidates(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 Model model) {
        List<Candidate> candidates;
        if (keyword != null && !keyword.isBlank()) {
            candidates = candidateRepository.findByFullNameContainingIgnoreCase(keyword);
        } else if (status != null && !status.isBlank()) {
            candidates = candidateRepository.findByStatus(status);
        } else {
            candidates = candidateRepository.findAll();
        }

        long newCount = candidateRepository.countByStatus("NEW");
        long screeningCount = candidateRepository.countByStatus("SCREENING");
        long interviewCount = candidateRepository.countByStatus("INTERVIEW");
        long hiredCount = candidateRepository.countByStatus("HIRED");

        model.addAttribute("candidates", candidates);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("newCount", newCount);
        model.addAttribute("screeningCount", screeningCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("hiredCount", hiredCount);
        return "hiring/candidate-list";
    }

    @GetMapping("/candidates/add")
    public String showAddCandidate(Model model) {
        model.addAttribute("candidate", new Candidate());
        model.addAttribute("postings", jobPostingRepository.findByStatus("OPEN"));
        return "hiring/candidate-form";
    }

    @GetMapping("/candidates/edit/{id}")
    public String showEditCandidate(@PathVariable Integer id, Model model) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ứng viên: " + id));
        model.addAttribute("candidate", candidate);
        model.addAttribute("postings", jobPostingRepository.findAll());
        return "hiring/candidate-form";
    }

    @PostMapping("/candidates/save")
    public String saveCandidate(@ModelAttribute Candidate candidate, RedirectAttributes ra) {
        candidateRepository.save(candidate);
        ra.addFlashAttribute("successMsg", "✅ Thông tin ứng viên đã được lưu!");
        return "redirect:/hiring/candidates";
    }

    @GetMapping("/candidates/status/{id}")
    public String updateCandidateStatus(@PathVariable Integer id,
                                        @RequestParam String status,
                                        RedirectAttributes ra) {
        Candidate candidate = candidateRepository.findById(id).orElseThrow();
        candidate.setStatus(status);
        candidateRepository.save(candidate);
        ra.addFlashAttribute("successMsg", "✅ Đã cập nhật trạng thái ứng viên.");
        return "redirect:/hiring/candidates";
    }

    @GetMapping("/candidates/delete/{id}")
    public String deleteCandidate(@PathVariable Integer id, RedirectAttributes ra) {
        candidateRepository.deleteById(id);
        ra.addFlashAttribute("successMsg", "🗑️ Đã xoá ứng viên.");
        return "redirect:/hiring/candidates";
    }

    /**
     * Chuyển ứng viên đã HIRED thành nhân viên chính thức trong hệ thống
     */
    @GetMapping("/candidates/hire/{id}")
    public String hireToEmployee(@PathVariable Integer id, RedirectAttributes ra) {
        Candidate c = candidateRepository.findById(id).orElseThrow();

        // Kiểm tra trạng thái phải là HIRED
        if (!"HIRED".equals(c.getStatus())) {
            ra.addFlashAttribute("errorMsg", "⚠️ Ứng viên chưa ở trạng thái HIRED!");
            return "redirect:/hiring/candidates";
        }

        // Tạo tài khoản nhân viên từ thông tin ứng viên
        // Username: tên viết tắt + "." + họ (không dấu, thường)
        String[] nameParts = c.getFullName().trim().split("\\s+");
        String lastName = nameParts[nameParts.length - 1].toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z]", "");
        String firstInitial = nameParts.length > 1 ? nameParts[0].substring(0, 1).toLowerCase() : "x";
        String baseUsername = lastName + "." + firstInitial;

        // Đảm bảo username unique
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix++;
        }

        User newEmployee = new User();
        newEmployee.setFullName(c.getFullName());
        newEmployee.setEmail(c.getEmail());
        newEmployee.setUsername(username);
        // Mật khẩu mặc định = 123456 (BCrypt)
        newEmployee.setPassword("$2a$10$hqVbLomRjVdJbGhyByGAeOYPaLYzGDxMIjilh3juV6.ZYc07DNkAu");
        newEmployee.setRole("USER");
        newEmployee.setStatus(UserStatus.ACTIVE);
        // Gán phòng ban từ job posting nếu có
        if (c.getJobPosting() != null && c.getJobPosting().getDepartment() != null) {
            newEmployee.setDepartment(c.getJobPosting().getDepartment());
        }

        userRepository.save(newEmployee);

        // Cập nhật candidate status -> đã tạo nhân viên
        c.setNotes((c.getNotes() != null ? c.getNotes() + " | " : "") +
                "[✅ Đã tạo tài khoản nhân viên: " + username + "]");
        candidateRepository.save(c);

        ra.addFlashAttribute("successMsg",
            "✅ Đã tạo tài khoản nhân viên cho " + c.getFullName() +
            "! Username: <strong>" + username + "</strong>, Mật khẩu: 123456");
        return "redirect:/hiring/candidates";
    }
}
