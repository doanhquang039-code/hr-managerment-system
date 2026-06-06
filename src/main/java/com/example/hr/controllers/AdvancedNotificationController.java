package com.example.hr.controllers;

import com.example.hr.dto.NotificationSendDTO;
import com.example.hr.enums.NotificationChannel;
import com.example.hr.models.NotificationPreference;
import com.example.hr.models.NotificationTemplate;
import com.example.hr.models.User;
import com.example.hr.repository.NotificationTemplateRepository;
import com.example.hr.service.AdvancedNotificationService;
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
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class AdvancedNotificationController {
    
    private final AdvancedNotificationService notificationService;
    private final NotificationTemplateRepository templateRepository;
    private final UserService userService;
    
    @GetMapping("/preferences")
    public String showPreferences(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        List<NotificationPreference> preferences = notificationService.getUserPreferences(user);
        
        model.addAttribute("preferences", preferences);
        model.addAttribute("channels", NotificationChannel.values());
        model.addAttribute("pageTitle", "Notification Preferences");
        
        return "user1/notification-preferences";
    }
    
    @PostMapping("/preferences/update")
    public String updatePreference(@RequestParam String notificationType,
                                  @RequestParam NotificationChannel channel,
                                  @RequestParam Boolean enabled,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(auth.getName());
        notificationService.updateNotificationPreference(user, notificationType, channel, enabled);
        
        redirectAttributes.addFlashAttribute("success", "Preference updated successfully");
        return "redirect:/notifications/preferences";
    }
    
    @GetMapping("/admin/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showTemplates(Model model) {
        List<NotificationTemplate> templates = notificationService.getAllTemplates();
        
        model.addAttribute("templates", templates);
        model.addAttribute("pageTitle", "Notification Templates");
        
        return "admin/notification-templates";
    }
    
    @GetMapping("/admin/templates/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showTemplateForm(Model model) {
        model.addAttribute("template", new NotificationTemplate());
        model.addAttribute("channels", NotificationChannel.values());
        model.addAttribute("pageTitle", "Create Notification Template");
        
        return "admin/notification-template-form";
    }
    
    @PostMapping("/admin/templates/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String saveTemplate(@ModelAttribute NotificationTemplate template,
                              RedirectAttributes redirectAttributes) {
        templateRepository.save(template);
        
        redirectAttributes.addFlashAttribute("success", "Template saved successfully");
        return "redirect:/notifications/admin/templates";
    }
    
    @GetMapping("/admin/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showSendForm(Model model) {
        List<NotificationTemplate> templates = notificationService.getAllTemplates();
        
        model.addAttribute("templates", templates);
        model.addAttribute("channels", NotificationChannel.values());
        model.addAttribute("pageTitle", "Send Notification");
        
        return "admin/notification-send";
    }
    
    @PostMapping("/admin/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseBody
    public String sendNotification(@RequestBody NotificationSendDTO dto) {
        notificationService.sendNotification(dto);
        return "Notifications sent successfully";
    }
}
