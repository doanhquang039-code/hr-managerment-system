package com.example.hr.controllers;







import com.example.hr.user.repository.UserRepository;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.leave.dto.LeaveBalanceDTO;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import com.example.hr.leave.service.AdvancedLeaveService;
import com.example.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/self-service")
@RequiredArgsConstructor
public class SelfServiceController {
    
    private final UserService userService;
    private final AdvancedLeaveService leaveService;
    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserRepository userRepository;
    
    @GetMapping("/portal")
    public String showPortal(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        
        // Get quick stats
        Integer currentYear = LocalDate.now().getYear();
        List<LeaveBalanceDTO> leaveBalances = leaveService.getUserLeaveBalances(user, currentYear);
        
        LocalDate today = LocalDate.now();
        boolean checkedInToday = attendanceRepository.existsByUserAndAttendanceDate(user, today);
        
        long pendingLeaves = leaveRequestRepository.countByUserAndStatus(user, LeaveStatus.PENDING);
        long activeTrainings = trainingEnrollmentRepository.countByUserAndStatusString(user, "IN_PROGRESS");
        
        model.addAttribute("user", user);
        model.addAttribute("leaveBalances", leaveBalances);
        model.addAttribute("checkedInToday", checkedInToday);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("activeTrainings", activeTrainings);
        model.addAttribute("pageTitle", "Employee Self-Service Portal");
        
        return "user1/self-service-portal";
    }
    
    @GetMapping("/payslips")
    public String showPayslips(Authentication auth, 
                              @RequestParam(required = false) Integer year,
                              Model model) {
        User user = userService.getUserByUsername(auth.getName());
        
        final Integer selectedYear = (year == null) ? LocalDate.now().getYear() : year;
        
        // Get payslips by user and year
        List<Payroll> payslips = payrollRepository.findByUser(user).stream()
                .filter(p -> p.getYear().equals(selectedYear))
                .sorted((p1, p2) -> {
                    int yearCompare = p2.getYear().compareTo(p1.getYear());
                    if (yearCompare != 0) return yearCompare;
                    return p2.getMonth().compareTo(p1.getMonth());
                })
                .toList();
        
        model.addAttribute("payslips", payslips);
        model.addAttribute("year", selectedYear);
        model.addAttribute("pageTitle", "My Payslips");
        
        return "user1/payslips";
    }
    
    @GetMapping("/payslips/{id}")
    public String showPayslipDetail(@PathVariable Long id, 
                                   Authentication auth,
                                   Model model) {
        User user = userService.getUserByUsername(auth.getName());
        Payroll payslip = payrollRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Payslip not found"));
        
        // Security check
        if (!payslip.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        model.addAttribute("payslip", payslip);
        model.addAttribute("pageTitle", "Payslip Detail");
        
        return "user1/payslip-detail";
    }
    
    @GetMapping("/documents")
    public String showDocuments(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        // TODO: Add findByUserOrderByCreatedAtDesc method to repository
        List<EmployeeDocument> documents = employeeDocumentRepository.findAll();
        
        model.addAttribute("documents", documents);
        model.addAttribute("pageTitle", "My Documents");
        
        return "user1/documents";
    }
    
    @GetMapping("/attendance-history")
    public String showAttendanceHistory(Authentication auth,
                                       @RequestParam(required = false) Integer year,
                                       @RequestParam(required = false) Integer month,
                                       Model model) {
        User user = userService.getUserByUsername(auth.getName());
        
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<Attendance> attendances = attendanceRepository
                .findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(user, startDate, endDate);
        
        model.addAttribute("attendances", attendances);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("pageTitle", "Attendance History");
        
        return "user1/attendance-history";
    }
    
    @GetMapping("/profile/edit")
    public String showProfileEdit(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile");
        
        return "user1/profile-edit";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(auth.getName());
        
        // Update allowed fields only
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setAddress(updatedUser.getAddress());
        // user.setEmergencyContact(updatedUser.getEmergencyContact());
        // user.setEmergencyPhone(updatedUser.getEmergencyPhone());
        
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/self-service/portal";
    }
    
    @GetMapping("/my-assets")
    public String showMyAssets(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        List<AssetAssignment> assignments = userAssetRepository.findByUserOrderByAssignedDateDesc(user);
        
        model.addAttribute("assignments", assignments);
        model.addAttribute("pageTitle", "My Assets");
        
        return "user1/my-assets";
    }
    
    @GetMapping("/request-document")
    public String showDocumentRequestForm(Model model) {
        model.addAttribute("documentTypes", new String[]{
            "Employment Certificate",
            "Salary Certificate",
            "Experience Letter",
            "Tax Document",
            "Other"
        });
        model.addAttribute("pageTitle", "Request Document");
        
        return "user1/document-request-form";
    }
    
    @PostMapping("/request-document")
    public String submitDocumentRequest(@RequestParam String documentType,
                                       @RequestParam String purpose,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(auth.getName());
        
        // TODO: Create document request entity and save
        // For now, just show success message
        
        redirectAttributes.addFlashAttribute("success", 
            "Document request submitted successfully. You will be notified when it's ready.");
        return "redirect:/self-service/portal";
    }
}
