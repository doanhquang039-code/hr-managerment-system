package com.example.hr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.Payment;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.repository.LeaveRequestRepository;
import com.example.hr.repository.PaymentRepository;
import com.example.hr.repository.TaskRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.PaymentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private LeaveRequestRepository leaveRepository;

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers       = userRepository.findByStatus(UserStatus.ACTIVE).size();
        long totalDepartments = departmentRepository.count();
        long totalPositions   = jobPositionRepository.findByActiveTrue().size();
        long pendingLeaves    = leaveRepository.findAllWithUser(null).stream()
                                    .filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        long totalTasks       = taskRepository.count();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("totalPositions", totalPositions);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        return "admin/dashboard";
    }
}