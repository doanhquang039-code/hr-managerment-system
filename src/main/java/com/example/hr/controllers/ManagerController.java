package com.example.hr.controllers;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.TaskStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.Attendance;
import com.example.hr.models.PerformanceReview;
import com.example.hr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private TaskAssignmentRepository taskAssignmentRepository;
    @Autowired private PerformanceReviewRepository reviewRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();

        // === BASIC STATS ===
        long totalEmployees = userRepository.findByStatus(UserStatus.ACTIVE).size();
        long pendingLeaves  = leaveRepository.countByStatus(LeaveStatus.PENDING);

        var allAssignments = taskAssignmentRepository.findAllWithUser();
        long activeTasks    = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completedTasks = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.COMPLETED).count();
        long pendingTasks   = allAssignments.stream().filter(a -> a.getStatus() == TaskStatus.PENDING).count();

        long checkedInToday = attendanceRepository.findByAttendanceDateBetween(today, today).size();
        long absentToday    = Math.max(0, totalEmployees - checkedInToday);

        // === CHART: Chấm công 7 ngày gần nhất ===
        List<String> attLabels  = new ArrayList<>();
        List<Integer> attPresent = new ArrayList<>();
        List<Integer> attLate    = new ArrayList<>();
        List<Integer> attAbsent  = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            attLabels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            List<Attendance> dayAtt = attendanceRepository.findByAttendanceDateBetween(date, date);
            long present = dayAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long late    = dayAtt.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            long absent  = Math.max(0, totalEmployees - dayAtt.size());
            attPresent.add((int) present);
            attLate.add((int) late);
            attAbsent.add((int) absent);
        }

        // === RECENT LEAVES (pending first) ===
        var allLeaves = leaveRepository.findAllWithUser(null);
        var pendingFirst = allLeaves.stream()
                .sorted((a, b) -> {
                    if (a.getStatus() == LeaveStatus.PENDING && b.getStatus() != LeaveStatus.PENDING) return -1;
                    if (a.getStatus() != LeaveStatus.PENDING && b.getStatus() == LeaveStatus.PENDING) return 1;
                    return 0;
                }).collect(Collectors.toList());

        // === TOP PERFORMERS ===
        List<PerformanceReview> topPerformers = reviewRepository.findAllWithUsers().stream()
                .filter(r -> r.getOverallScore() != null)
                .sorted((a, b) -> b.getOverallScore().compareTo(a.getOverallScore()))
                .limit(5)
                .collect(Collectors.toList());

        // Model attributes
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("pendingLeaves",  pendingLeaves);
        model.addAttribute("activeTasks",    activeTasks);
        model.addAttribute("absentToday",    absentToday);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("pendingTasks",   pendingTasks);
        model.addAttribute("recentLeaves",   pendingFirst);
        model.addAttribute("recentTasks",    taskRepository.findAll());
        model.addAttribute("teamMembers",    userRepository.findByStatus(UserStatus.ACTIVE));
        model.addAttribute("topPerformers",  topPerformers);
        model.addAttribute("attLabels",      attLabels);
        model.addAttribute("attPresent",     attPresent);
        model.addAttribute("attLate",        attLate);
        model.addAttribute("attAbsent",      attAbsent);
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")));

        return "manager/dashboard";
    }
}