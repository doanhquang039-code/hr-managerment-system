package com.example.hr.controllers;

import com.example.hr.enums.AnnouncementPriority;
import com.example.hr.models.CompanyAnnouncement;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.CloudStorageFacade;
import com.example.hr.service.CompanyAnnouncementService;
import com.example.hr.service.EmailFacade;
import com.example.hr.enums.UserStatus;
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
    private final EmailFacade emailFacade;
    private final UserRepository userRepository;
    private final CloudStorageFacade cloudStorageFacade;

    public AdminAnnouncementController(CompanyAnnouncementService announcementService,
                                       DepartmentRepository departmentRepository,
                                       AuthUserHelper authUserHelper,
                                       EmailFacade emailFacade,
                                       UserRepository userRepository,
                                       CloudStorageFacade cloudStorageFacade) {
        this.announcementService = announcementService;
        this.departmentRepository = departmentRepository;
        this.authUserHelper = authUserHelper;
        this.emailFacade = emailFacade;
        this.userRepository = userRepository;
        this.cloudStorageFacade = cloudStorageFacade;
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
            if (author == null) return "redirect:/login";
            CompanyAnnouncement saved = announcementService.create(
                    author, title.trim(), content.trim(), departmentId, p, published, activeFlag);
            ra.addFlashAttribute("success", "Đã tạo thông báo.");

            // Gửi email broadcast nếu active
            if (activeFlag) {
                boolean sendEmail = "true".equalsIgnoreCase(
                        ((org.springframework.web.context.request.ServletRequestAttributes)
                                org.springframework.web.context.request.RequestContextHolder
                                        .getRequestAttributes())
                                .getRequest().getParameter("sendEmail"));
                if (sendEmail) {
                    userRepository.findByStatus(UserStatus.ACTIVE).forEach(u -> {
                        if (u.getEmail() != null && !u.getEmail().isBlank()) {
                            emailFacade.sendAnnouncement(u.getEmail(), u.getFullName(),
                                    title.trim(), content.trim());
                        }
                    });
                    // Firebase broadcast
                    cloudStorageFacade.broadcastAnnouncement(title.trim(), content.trim());
                    ra.addFlashAttribute("success", "Đã tạo thông báo và gửi email đến tất cả nhân viên!");
                }
            }
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
