package com.example.hr.recruitment.controller;

import com.example.hr.recruitment.entity.Candidate;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.recruitment.service.CandidateService;
import com.example.hr.recruitment.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/hiring/candidates", "/admin/candidates"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR','HIRING','MANAGER')")
public class CandidateController {

    private final CandidateService candidateService;
    private final JobPostingService jobPostingService;

    @GetMapping
    public String listCandidates(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? 
                org.springframework.data.domain.Sort.by(sortBy).ascending() : 
                org.springframework.data.domain.Sort.by(sortBy).descending();
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        org.springframework.data.domain.Page<Candidate> candidatePage = candidateService.searchCandidates(search, stage, pageable);
        
        var statistics = candidateService.getCandidateStatistics();
        
        model.addAttribute("candidatePage", candidatePage);
        model.addAttribute("candidates", candidatePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", candidatePage.getTotalPages());
        model.addAttribute("totalItems", candidatePage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("statistics", statistics);
        model.addAttribute("selectedStage", stage);
        model.addAttribute("searchKeyword", search);
        
        return "hiring/candidates/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteCandidate(@PathVariable Integer id, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.deleteCandidate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting candidate: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates";
    }

    @GetMapping("/create")
    public String createCandidateForm(Model model) {
        model.addAttribute("candidate", new Candidate());
        model.addAttribute("jobPostings", jobPostingService.getActiveJobPostings());
        return "hiring/candidates/create";
    }

    @PostMapping("/create")
    public String createCandidate(@ModelAttribute Candidate candidate, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.createCandidate(candidate);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating candidate: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates";
    }

    @GetMapping("/{id}")
    public String viewCandidate(@PathVariable Integer id, Model model) {
        var candidate = candidateService.getCandidateById(id);
        model.addAttribute("candidate", candidate);
        return "hiring/candidates/view";
    }

    @GetMapping("/{id}/edit")
    public String editCandidateForm(@PathVariable Integer id, Model model) {
        var candidate = candidateService.getCandidateById(id);
        model.addAttribute("candidate", candidate);
        model.addAttribute("jobPostings", jobPostingService.getActiveJobPostings());
        return "hiring/candidates/edit";
    }

    @PostMapping("/{id}/edit")
    public String editCandidate(@PathVariable Integer id, @ModelAttribute Candidate candidate, 
                                RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.updateCandidate(id, candidate);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating candidate: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates/" + id;
    }

    @PostMapping("/{id}/move-stage")
    public String moveToStage(@PathVariable Integer id, @RequestParam String stage, 
                              RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.moveToStage(id, stage);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate moved to " + stage + " stage!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error moving candidate: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates/" + id;
    }

    @PostMapping("/{id}/score")
    public String updateScore(@PathVariable Integer id, @RequestParam Integer score, 
                              RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.updateScore(id, score);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate score updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating score: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates/" + id;
    }

    @PostMapping("/{id}/notes")
    public String addNotes(@PathVariable Integer id, @RequestParam String notes, 
                            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.addNotes(id, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Notes added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding notes: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates/" + id;
    }

    @PostMapping("/{id}/hire")
    public String hireCandidate(@PathVariable Integer id, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.hireCandidate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate hired successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error hiring candidate: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectCandidate(@PathVariable Integer id, @RequestParam String reason, 
                                  RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            candidateService.rejectCandidate(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Candidate rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting candidate: " + e.getMessage());
        }
        return "redirect:" + HiringPathHelper.getRedirectPrefix(request) + "/candidates/" + id;
    }
}

