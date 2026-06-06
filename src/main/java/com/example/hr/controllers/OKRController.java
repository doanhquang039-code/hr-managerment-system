package com.example.hr.controllers;

import com.example.hr.dto.OKRCreateDTO;
import com.example.hr.enums.OKRStatus;
import com.example.hr.models.KeyResult;
import com.example.hr.models.Objective;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.KeyResultRepository;
import com.example.hr.repository.ObjectiveRepository;
import com.example.hr.service.OKRService;
import com.example.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OKRController {
    
    private final OKRService okrService;
    private final ObjectiveRepository objectiveRepository;
    private final KeyResultRepository keyResultRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;
    
    @GetMapping("/okr/my-objectives")
    public String showMyObjectives(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        List<Objective> objectives = okrService.getUserObjectives(user);
        
        model.addAttribute("objectives", objectives);
        model.addAttribute("pageTitle", "My OKRs");
        
        return "user1/okr-list";
    }
    
    @GetMapping("/okr/company-objectives")
    public String showCompanyObjectives(Model model) {
        List<Objective> objectives = okrService.getCompanyObjectives();
        
        model.addAttribute("objectives", objectives);
        model.addAttribute("pageTitle", "Company OKRs");
        
        return "user1/okr-company";
    }
    
    @GetMapping("/admin/okr/objectives")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public String showAllObjectives(@RequestParam(required = false) String level,
                                   @RequestParam(required = false) String status,
                                   Model model) {
        List<Objective> objectives;
        
        if (level != null && status != null) {
            objectives = objectiveRepository.findByLevelAndStatusOrderByCreatedAtDesc(
                    level, OKRStatus.valueOf(status));
        } else if (status != null) {
            objectives = objectiveRepository.findByStatusOrderByCreatedAtDesc(OKRStatus.valueOf(status));
        } else {
            objectives = objectiveRepository.findAll();
        }
        
        model.addAttribute("objectives", objectives);
        model.addAttribute("pageTitle", "OKR Management");
        model.addAttribute("levels", new String[]{"COMPANY", "DEPARTMENT", "INDIVIDUAL"});
        model.addAttribute("statuses", OKRStatus.values());
        
        return "admin/okr-list";
    }
    
    @GetMapping("/admin/okr/objectives/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public String showObjectiveForm(Model model) {
        model.addAttribute("objective", new Objective());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("levels", new String[]{"COMPANY", "DEPARTMENT", "INDIVIDUAL"});
        model.addAttribute("pageTitle", "Create Objective");
        
        return "admin/okr-form";
    }
    
    @PostMapping("/admin/okr/objectives/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public String saveObjective(@ModelAttribute OKRCreateDTO dto, 
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(auth.getName());
        okrService.createObjective(dto, user);
        
        redirectAttributes.addFlashAttribute("success", "Objective created successfully");
        return "redirect:/admin/okr/objectives";
    }
    
    @GetMapping("/okr/objectives/{id}")
    public String showObjectiveDetail(@PathVariable Long id, Model model) {
        Objective objective = objectiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objective not found"));
        
        List<KeyResult> keyResults = keyResultRepository.findByObjectiveIdOrderByCreatedAtAsc(id);
        
        model.addAttribute("objective", objective);
        model.addAttribute("keyResults", keyResults);
        model.addAttribute("pageTitle", objective.getTitle());
        
        return "user1/okr-detail";
    }
    
    @PostMapping("/okr/key-results/{id}/update-progress")
    public String updateKeyResultProgress(@PathVariable Long id,
                                         @RequestParam Double newValue,
                                         @RequestParam(required = false) String notes,
                                         Authentication auth,
                                         RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(auth.getName());
        KeyResult keyResult = okrService.updateKeyResultProgress(id, newValue, notes, user);
        
        redirectAttributes.addFlashAttribute("success", "Progress updated successfully");
        return "redirect:/okr/objectives/" + keyResult.getObjective().getId();
    }
    
    @PostMapping("/admin/okr/objectives/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public String updateObjectiveStatus(@PathVariable Long id,
                                       @RequestParam String status,
                                       RedirectAttributes redirectAttributes) {
        okrService.updateObjectiveStatus(id, OKRStatus.valueOf(status));
        
        redirectAttributes.addFlashAttribute("success", "Status updated successfully");
        return "redirect:/admin/okr/objectives";
    }
}
