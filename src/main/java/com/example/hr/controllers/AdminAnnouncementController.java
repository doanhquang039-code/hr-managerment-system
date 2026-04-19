package com.example.hr.controllers;

import com.example.hr.enums.AnnouncementPriority;
import com.example.hr.models.CompanyAnnouncement;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.CompanyAnnouncementService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/announcements")
public class AdminAnnouncementController {

    private final CompanyAnnouncementService announcementService;
    private final DepartmentRepository departmentRepository;
    private final AuthUserHelper authUserHelper;

    public AdminAnnouncementController(CompanyAnnouncementService announcementService,
                                       DepartmentRepository departmentRepository,
                                       AuthUserHelper authUserHelper) {
        this.announcementService = announcementService;
        this.departmentRepository = departmentRepository;
        this.authUserHelper = authUserHelper;
    }

    @GetMapping
    public String list(Model model) {
        List<CompanyAnnouncement> items = announcementService.listAllForAdmin();
        model.addAttribute("announcements", items);
        return "admin/announcement-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("announcement", new CompanyAnnouncement());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("priorities", AnnouncementPriority.values());
        return "admin/announcement-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        CompanyAnnouncement a = announcementService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        model.addAttribute("announcement", a);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("priorities", AnnouncementPriority.values());
        return "admin/announcement-form";
    }

    @PostMapping
    public String save(
            Authentication auth,
            @RequestParam(required = false) Integer id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(defaultValue = "NORMAL") String priority,
            @RequestParam(required = false) String publishedAt,
            @RequestParam(required = false) String active,
            RedirectAttributes ra) {
        AnnouncementPriority p = parsePriority(priority);
        LocalDateTime published = parseDateTime(publishedAt);
        boolean activeFlag = "true".equalsIgnoreCase(active) || "on".equalsIgnoreCase(active);
        if (id == null) {
            User author = authUserHelper.getCurrentUser(auth);
            if (author == null) {
                return "redirect:/login";
            }
            announcementService.create(author, title.trim(), content.trim(), departmentId, p, published, activeFlag);
            ra.addFlashAttribute("success", "Đã tạo thông báo.");
        } else {
            announcementService.update(id, title.trim(), content.trim(), departmentId, p, published, activeFlag);
            ra.addFlashAttribute("success", "Đã cập nhật thông báo.");
        }
        return "redirect:/admin/announcements";
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        announcementService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa thông báo.");
        return "redirect:/admin/announcements";
    }

    private static AnnouncementPriority parsePriority(String raw) {
        try {
            return AnnouncementPriority.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            return AnnouncementPriority.NORMAL;
        }
    }
}
