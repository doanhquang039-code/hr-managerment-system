package com.example.hr.controllers;

import com.example.hr.models.CompanyAnnouncement;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.CompanyAnnouncementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user1/announcements")
@PreAuthorize("isAuthenticated()")
public class UserAnnouncementController {

    private final CompanyAnnouncementService announcementService;
    private final AuthUserHelper authUserHelper;

    public UserAnnouncementController(CompanyAnnouncementService announcementService,
                                        AuthUserHelper authUserHelper) {
        this.announcementService = announcementService;
        this.authUserHelper = authUserHelper;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }
        List<CompanyAnnouncement> list = announcementService.listForEmployee(user);
        model.addAttribute("user", user);
        model.addAttribute("announcements", list);
        return "user1/announcements";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }
        return announcementService.findByIdForEmployee(id, user)
                .map(a -> {
                    model.addAttribute("user", user);
                    model.addAttribute("announcement", a);
                    return "user1/announcement-detail";
                })
                .orElse("redirect:/user1/announcements");
    }
}
