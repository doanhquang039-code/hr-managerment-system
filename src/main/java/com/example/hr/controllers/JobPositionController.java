package com.example.hr.controllers;

import com.example.hr.models.JobPosition;
import com.example.hr.repository.JobPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin/positions")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class JobPositionController {

    @Autowired
    private JobPositionRepository positionRepository;

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "level", required = false) Integer level,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "id") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? 
                org.springframework.data.domain.Sort.by(sortBy).ascending() : 
                org.springframework.data.domain.Sort.by(sortBy).descending();
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        org.springframework.data.domain.Page<JobPosition> positionPage = positionRepository.searchPositions(keyword, level, pageable);

        model.addAttribute("positionPage", positionPage);
        model.addAttribute("positions", positionPage.getContent());
        model.addAttribute("allPositions", positionRepository.findByActiveTrue());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", positionPage.getTotalPages());
        model.addAttribute("totalItems", positionPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedLevel", level);
        return "admin/position-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("position", new JobPosition());
        model.addAttribute("allPositions", positionRepository.findByActiveTrue());
        return "admin/position-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        JobPosition pos = positionRepository.findById(id).orElseThrow();
        model.addAttribute("position", pos);
        model.addAttribute("allPositions", positionRepository.findByActiveTrue());
        return "admin/position-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("position") JobPosition pos) {
        positionRepository.save(pos);
        return "redirect:/admin/positions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        JobPosition pos = positionRepository.findById(id).orElseThrow();
        pos.setActive(false);
        positionRepository.save(pos);
        return "redirect:/admin/positions";
    }
}
