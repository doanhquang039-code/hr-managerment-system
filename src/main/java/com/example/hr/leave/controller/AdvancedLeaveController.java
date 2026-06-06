package com.example.hr.leave.controller;

import com.example.hr.leave.dto.LeaveBalanceDTO;
import com.example.hr.enums.LeaveType;
import com.example.hr.models.PublicHoliday;
import com.example.hr.models.User;
import com.example.hr.repository.PublicHolidayRepository;
import com.example.hr.leave.service.AdvancedLeaveService;
import com.example.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/leave")
@RequiredArgsConstructor
public class AdvancedLeaveController {
    
    private final AdvancedLeaveService leaveService;
    private final PublicHolidayRepository holidayRepository;
    private final UserService userService;
    
    @GetMapping("/balances")
    public String showLeaveBalances(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        Integer currentYear = LocalDate.now().getYear();
        
        List<LeaveBalanceDTO> balances = leaveService.getUserLeaveBalances(user, currentYear);
        
        model.addAttribute("balances", balances);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("pageTitle", "My Leave Balances");
        
        return "user1/leave-balances";
    }
    
    @GetMapping("/calendar")
    public String showLeaveCalendar(@RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Integer month,
                                   Model model) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        Map<String, Object> calendar = leaveService.getLeaveCalendar(year, month);
        
        model.addAttribute("calendar", calendar);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("pageTitle", "Leave Calendar");
        
        return "user1/leave-calendar";
    }
    
    @GetMapping("/admin/holidays")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showHolidays(@RequestParam(required = false) Integer year, Model model) {
        if (year == null) year = LocalDate.now().getYear();
        
        List<PublicHoliday> holidays = holidayRepository.findByYearAndIsActiveOrderByDateAsc(year, true);
        
        model.addAttribute("holidays", holidays);
        model.addAttribute("year", year);
        model.addAttribute("pageTitle", "Public Holidays Management");
        
        return "admin/holiday-list";
    }
    
    @GetMapping("/admin/holidays/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showHolidayForm(Model model) {
        model.addAttribute("holiday", new PublicHoliday());
        model.addAttribute("pageTitle", "Add Public Holiday");
        return "admin/holiday-form";
    }
    
    @PostMapping("/admin/holidays/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String saveHoliday(@ModelAttribute PublicHoliday holiday, RedirectAttributes redirectAttributes) {
        holidayRepository.save(holiday);
        redirectAttributes.addFlashAttribute("success", "Holiday saved successfully");
        return "redirect:/leave/admin/holidays";
    }
    
    @GetMapping("/admin/holidays/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String editHoliday(@PathVariable Long id, Model model) {
        PublicHoliday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        
        model.addAttribute("holiday", holiday);
        model.addAttribute("pageTitle", "Edit Public Holiday");
        return "admin/holiday-form";
    }
    
    @GetMapping("/admin/holidays/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        holidayRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Holiday deleted successfully");
        return "redirect:/leave/admin/holidays";
    }
}
