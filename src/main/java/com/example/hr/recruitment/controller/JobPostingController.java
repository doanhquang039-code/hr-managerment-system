package com.example.hr.recruitment.controller;


import com.example.hr.department.entity.Department;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.recruitment.service.JobPostingService;
import com.example.hr.department.service.DepartmentService;
import com.example.hr.recruitment.service.JobPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hiring/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR','HIRING','MANAGER')")
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final DepartmentService departmentService;
    private final JobPositionService jobPositionService;

    @GetMapping
    public String listJobPostings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        String normalizedSortBy = normalizeJobSort(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, normalizedSortBy));
        var jobPage = jobPostingService.searchJobPostings(blankToNull(search), blankToNull(status),
                blankToNull(employmentType), blankToNull(experienceLevel), departmentId, pageable);
        
        var statistics = jobPostingService.getJobPostingStatistics();
        
        model.addAttribute("jobPage", jobPage);
        model.addAttribute("jobs", jobPage.getContent());
        model.addAttribute("currentPage", jobPage.getNumber());
        model.addAttribute("totalPages", jobPage.getTotalPages());
        model.addAttribute("totalItems", jobPage.getTotalElements());
        model.addAttribute("sortField", normalizedSortBy);
        model.addAttribute("sortDir", direction.name().toLowerCase());
        model.addAttribute("reverseSortDir", direction == Sort.Direction.ASC ? "desc" : "asc");
        model.addAttribute("statistics", statistics);
        model.addAttribute("activeJobs", statistics.activeJobs());
        model.addAttribute("draftJobs", statistics.draftJobs());
        model.addAttribute("closedJobs", statistics.closedJobs());
        model.addAttribute("totalApplications", statistics.totalApplications());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedEmploymentType", employmentType);
        model.addAttribute("selectedExperienceLevel", experienceLevel);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("departments", departmentService.getAllDepartments());
        
        return "hiring/jobs/list";
    }

    @GetMapping("/create")
    public String createJobForm(Model model) {
        model.addAttribute("job", new JobPosting());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("positions", jobPositionService.getAllPositions());
        return "hiring/jobs/create";
    }

    @PostMapping("/create")
    public String createJob(@ModelAttribute JobPosting job, RedirectAttributes redirectAttributes) {
        try {
            jobPostingService.createJobPosting(job);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating job posting: " + e.getMessage());
        }
        return "redirect:/hiring/jobs";
    }

    @GetMapping("/{id}")
    public String viewJob(@PathVariable Integer id, Model model) {
        var job = jobPostingService.getJobPostingById(id);
        jobPostingService.incrementViewCount(id);
        model.addAttribute("job", job);
        return "hiring/jobs/view";
    }

    @GetMapping("/{id}/edit")
    public String editJobForm(@PathVariable Integer id, Model model) {
        var job = jobPostingService.getJobPostingById(id);
        model.addAttribute("job", job);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("positions", jobPositionService.getAllPositions());
        return "hiring/jobs/edit";
    }

    @PostMapping("/{id}/edit")
    public String editJob(@PathVariable Integer id, @ModelAttribute JobPosting job, 
                         RedirectAttributes redirectAttributes) {
        try {
            jobPostingService.updateJobPosting(id, job);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating job posting: " + e.getMessage());
        }
        return "redirect:/hiring/jobs/" + id;
    }

    @PostMapping("/{id}/publish")
    public String publishJob(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            jobPostingService.publishJobPosting(id);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error publishing job posting: " + e.getMessage());
        }
        return "redirect:/hiring/jobs/" + id;
    }

    @PostMapping("/{id}/close")
    public String closeJob(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            jobPostingService.closeJobPosting(id);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting closed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error closing job posting: " + e.getMessage());
        }
        return "redirect:/hiring/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteJob(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            jobPostingService.deleteJobPosting(id);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting job posting: " + e.getMessage());
        }
        return "redirect:/hiring/jobs";
    }

    @GetMapping("/closing-soon")
    public String jobsClosingSoon(Model model) {
        var jobs = jobPostingService.getJobsClosingSoon(14);
        model.addAttribute("jobs", jobs);
        model.addAttribute("closingSoonJobs", jobs);
        model.addAttribute("totalClosingSoon", jobs.size());
        return "hiring/jobs/closing-soon";
    }

    private String normalizeJobSort(String sortBy) {
        return switch (sortBy) {
            case "title", "status", "employmentType", "experienceLevel", "location",
                 "postingDate", "closingDate", "viewsCount", "applicationsCount", "createdAt" -> sortBy;
            default -> "createdAt";
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}


