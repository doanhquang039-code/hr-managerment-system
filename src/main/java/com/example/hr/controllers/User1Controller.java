package com.example.hr.controllers;

import com.example.hr.models.User;
import com.example.hr.repository.*;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user1") // Đường dẫn riêng dành cho Nhân viên
public class User1Controller {

    @Autowired
    private PayrollRepository payrollRepository;
    @Autowired
    private LeaveRequestRepository leaveRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

  @GetMapping("/dashboard")
public String dashboard(Authentication authentication, Model model) {
    String email = "";
    
    if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
        // Nếu là Google Login
        org.springframework.security.oauth2.core.user.OAuth2User oAuth2User = 
            (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
        email = oAuth2User.getAttribute("email");
    } else {
        // Nếu là Login thường
        email = authentication.getName();
    }

    // Tìm user trong DB theo Email lấy từ Google
    User user = userRepository.findByEmail(email).orElse(null);
    
    if (user == null) {
        // Nếu login Google thành công nhưng Email này chưa có trong bảng User của mình
        return "redirect:/login?error=user_not_found";
    }

    model.addAttribute("user", user);
    return "user1/dashboard"; // Đảm bảo file này tồn tại trong templates/user/
}
}