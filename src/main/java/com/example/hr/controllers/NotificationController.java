package com.example.hr.controllers;

import com.example.hr.models.Notification;
import com.example.hr.models.User;
import com.example.hr.enums.Role;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping({"/notifications", "/user1/notifications", "/manager/notifications", "/hiring/notifications", "/admin/notifications"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUserHelper authUserHelper;

    private User getCurrentUser(Authentication auth) {
        return authUserHelper.getCurrentUser(auth);
    }

    private String getNotificationRedirect(User user) {
        if (user == null) return "/login";
        if (user.getRole() == Role.ADMIN) return "/admin/notifications";
        if (user.getRole() == Role.MANAGER) return "/manager/notifications";
        if (user.getRole() == Role.HIRING) return "/hiring/notifications";
        return "/user1/notifications";
    }

    @GetMapping
    public String listNotifications(HttpServletRequest request, Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login";
        }

        String servletPath = request.getServletPath();
        String correctRedirect = getNotificationRedirect(user);
        if (!servletPath.equals(correctRedirect)) {
            return "redirect:" + correctRedirect;
        }

        model.addAttribute("notifications", notificationService.getAll(user));
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        model.addAttribute("user", user);
        model.addAttribute("markAllReadAction", correctRedirect + "/mark-all-read");
        return "user1/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead(Authentication auth) {
        User user = getCurrentUser(auth);
        if (user != null) {
            notificationService.markAllRead(user);
        }
        return "redirect:" + getNotificationRedirect(user);
    }

    @GetMapping("/read/{id}")
    public String markRead(@PathVariable Integer id, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user != null) {
            notificationService.markRead(id, user);
        }
        return "redirect:" + getNotificationRedirect(user);
    }

    @GetMapping("/count")
    @ResponseBody
    public long getUnreadCount(Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return 0;
        }
        return notificationService.countUnread(user);
    }

    @GetMapping("/api/list")
    @ResponseBody
    public List<Notification> getNotificationList(
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) {
            return List.of();
        }
        return notificationService.getRecent(user, limit);
    }

    @PutMapping("/api/mark-all-read")
    @ResponseBody
    public java.util.Map<String, Object> markAllReadApi(Authentication auth) {
        User user = getCurrentUser(auth);
        if (user != null) {
            notificationService.markAllRead(user);
        }
        return java.util.Map.of("success", true);
    }
}


