package com.example.hr.controllers;

import com.example.hr.models.Meeting;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.MeetingService;
import com.example.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/meetings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerMeetingController {

    private final MeetingService meetingService;
    private final UserService userService;
    private final AuthUserHelper authUserHelper;

    /**
     * List meetings
     */
    @GetMapping
    public String listMeetings(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        var meetings = meetingService.getMeetingsForUser(manager);
        var upcomingMeetings = meetingService.getUpcomingMeetings(manager);
        var meetingStats = meetingService.getMeetingStatistics(manager);
        
        model.addAttribute("meetings", meetings);
        model.addAttribute("upcomingMeetings", upcomingMeetings);
        model.addAttribute("meetingStats", meetingStats);

        return "manager/meetings/list";
    }

    /**
     * Show create meeting form
     */
    @GetMapping("/create")
    public String createMeetingForm(Authentication authentication, Model model) {
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";

        model.addAttribute("meeting", new Meeting());
        
        // Get team members for participant selection
        if (manager.getDepartment() != null) {
            var teamMembers = userService.getUsersByDepartment(manager.getDepartment());
            model.addAttribute("teamMembers", teamMembers);
        }

        return "manager/meetings/create";
    }

    /**
     * Create new meeting
     */
    @PostMapping("/create")
    public String createMeeting(
            @ModelAttribute Meeting meeting,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User manager = authUserHelper.getCurrentUser(authentication);
            if (manager == null) throw new RuntimeException("Not authenticated");
            meeting.setOrganizer(manager);
            meeting.setDepartment(manager.getDepartment());
            
            meetingService.createMeeting(meeting, authentication);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Meeting scheduled successfully: " + meeting.getTitle());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to create meeting: " + e.getMessage());
        }

        return "redirect:/manager/meetings";
    }

    /**
     * Show edit meeting form
     */
    @GetMapping("/edit/{id}")
    public String editMeetingForm(@PathVariable Integer id, Authentication authentication, Model model) {
        try {
            User manager = authUserHelper.getCurrentUser(authentication);
            if (manager == null) return "redirect:/login";
            var meeting = meetingService.getMeetingById(id);
            
            // Check if manager is the organizer
            if (!meeting.getOrganizer().getId().equals(manager.getId())) {
                model.addAttribute("errorMessage", "You can only edit meetings you organized");
                return "redirect:/manager/meetings";
            }
            
            model.addAttribute("meeting", meeting);
            
            // Get team members for participant selection
            if (manager.getDepartment() != null) {
                var teamMembers = userService.getUsersByDepartment(manager.getDepartment());
                model.addAttribute("teamMembers", teamMembers);
            }
            
            return "manager/meetings/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Meeting not found");
            return "redirect:/manager/meetings";
        }
    }

    /**
     * Update meeting
     */
    @PostMapping("/edit/{id}")
    public String updateMeeting(
            @PathVariable Integer id,
            @ModelAttribute Meeting meetingData,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            meetingService.updateMeeting(id, meetingData, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "Meeting updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to update meeting: " + e.getMessage());
        }

        return "redirect:/manager/meetings";
    }

    /**
     * Complete meeting
     */
    @PostMapping("/complete/{id}")
    public String completeMeeting(
            @PathVariable Integer id,
            @RequestParam String notes,
            @RequestParam(required = false) String actionItems,
            RedirectAttributes redirectAttributes) {
        
        try {
            meetingService.completeMeeting(id, notes, actionItems);
            redirectAttributes.addFlashAttribute("successMessage", "Meeting marked as completed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to complete meeting: " + e.getMessage());
        }

        return "redirect:/manager/meetings";
    }

    /**
     * Cancel meeting
     */
    @PostMapping("/cancel/{id}")
    public String cancelMeeting(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            meetingService.cancelMeeting(id);
            redirectAttributes.addFlashAttribute("successMessage", "Meeting cancelled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to cancel meeting: " + e.getMessage());
        }

        return "redirect:/manager/meetings";
    }

    /**
     * View meeting details
     */
    @GetMapping("/view/{id}")
    public String viewMeeting(@PathVariable Integer id, Model model) {
        try {
            var meeting = meetingService.getMeetingById(id);
            model.addAttribute("meeting", meeting);
            return "manager/meetings/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Meeting not found");
            return "redirect:/manager/meetings";
        }
    }

    /**
     * Get meetings by type
     */
    @GetMapping("/type/{type}")
    public String getMeetingsByType(
            @PathVariable String type,
            Authentication authentication,
            Model model) {
        
        User manager = authUserHelper.getCurrentUser(authentication);
        if (manager == null) return "redirect:/login";
        var meetings = meetingService.getMeetingsByType(type, manager);
        
        model.addAttribute("meetings", meetings);
        model.addAttribute("meetingType", type);

        return "manager/meetings/by-type";
    }
}


