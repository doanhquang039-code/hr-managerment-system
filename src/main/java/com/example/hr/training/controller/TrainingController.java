package com.example.hr.training.controller;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.TrainingStatus;
import com.example.hr.training.entity.TrainingProgram;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/training")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class TrainingController {

    @Autowired
    private TrainingProgramRepository trainingProgramRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String trainingType,
                       Model model) {
        List<TrainingProgram> programs = trainingProgramRepository.findAll();

        // Keyword search
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            programs = programs.stream()
                    .filter(p -> p.getProgramName().toLowerCase().contains(kw)
                            || (p.getInstructor() != null && p.getInstructor().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        // Status filter
        if (status != null && !status.isBlank()) {
            try {
                TrainingStatus ts = TrainingStatus.valueOf(status);
                programs = programs.stream()
                        .filter(p -> p.getStatus() == ts)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }

        // Training type filter
        if (trainingType != null && !trainingType.isBlank()) {
            programs = programs.stream()
                    .filter(p -> trainingType.equals(p.getTrainingType()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("programs", programs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", trainingType);
        model.addAttribute("statuses", TrainingStatus.values());
        return "admin/training-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("program", new TrainingProgram());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("statuses", TrainingStatus.values());
        return "admin/training-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        TrainingProgram program = trainingProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o: " + id));
        model.addAttribute("program", program);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("statuses", TrainingStatus.values());
        return "admin/training-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("program") TrainingProgram program, RedirectAttributes ra) {
        trainingProgramRepository.save(program);
        ra.addFlashAttribute("successMsg", "âœ… ChÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o Ä‘Ã£ Ä‘Æ°á»£c lÆ°u!");
        return "redirect:/admin/training";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        trainingProgramRepository.deleteById(id);
        ra.addFlashAttribute("successMsg", "ðŸ—‘ï¸ ÄÃ£ xÃ³a chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.");
        return "redirect:/admin/training";
    }
}

