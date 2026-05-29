package com.example.hr.controllers;

import com.example.hr.models.*;
import com.example.hr.service.AdvancedAttendanceService;
import com.example.hr.service.AuthUserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttendanceAdvancedController {
    
    private final AdvancedAttendanceService attendanceService;
    private final AuthUserHelper authUserHelper;
    
    // ===== Geofencing Check-in =====
    
    @GetMapping("/attendance/advanced/checkin")
    @PreAuthorize("isAuthenticated()")
    public String checkinPage(Model model) {
        model.addAttribute("locations", attendanceService.getActiveLocations());
        return "attendance/geofencing-checkin";
    }
    
    @PostMapping("/attendance/advanced/checkin/validate")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public boolean validateLocation(@RequestParam Double latitude,
                                    @RequestParam Double longitude,
                                    @RequestParam Integer locationId) {
        return attendanceService.validateGeofencing(latitude, longitude, locationId);
    }
    
    // ===== Face Recognition =====
    
    @GetMapping("/attendance/advanced/face-setup")
    @PreAuthorize("isAuthenticated()")
    public String faceSetupPage(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        model.addAttribute("hasFaceData", attendanceService.hasFaceRecognition(user));
        return "attendance/face-setup";
    }
    
    @PostMapping("/attendance/advanced/face-register")
    @PreAuthorize("isAuthenticated()")
    public String registerFace(@RequestParam String faceEncoding,
                              @RequestParam String imageUrl,
                              Authentication auth,
                              RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            attendanceService.registerFaceData(user, faceEncoding, imageUrl);
            ra.addFlashAttribute("success", "Đăng ký khuôn mặt thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendance/advanced/face-setup";
    }
    
    // ===== Shift Management (Admin) =====
    
    @GetMapping("/admin/shifts")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String shiftList(@RequestParam(name = "period", required = false) String period, Model model) {
        List<Shift> shifts = attendanceService.getActiveShifts();
        if (period != null && !period.isBlank()) {
            shifts = shifts.stream()
                    .filter(shift -> matchesShiftPeriod(shift, period))
                    .toList();
        }
        model.addAttribute("shifts", shifts);
        model.addAttribute("selectedPeriod", period);
        return "admin/shift-list";
    }
    
    @GetMapping("/admin/shifts/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String newShiftForm(@RequestParam(name = "period", required = false) String period, Model model) {
        Shift shift = new Shift();
        applyPeriodDefaults(shift, period);
        model.addAttribute("shift", shift);
        model.addAttribute("selectedPeriod", period);
        return "admin/shift-form";
    }

    @GetMapping("/admin/shifts/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String editShiftForm(@PathVariable Integer id, Model model) {
        model.addAttribute("shift", attendanceService.getShiftById(id));
        return "admin/shift-form";
    }
    
    @PostMapping("/admin/shifts/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String saveShift(@ModelAttribute Shift shift, RedirectAttributes ra) {
        try {
            attendanceService.createShift(shift);
            ra.addFlashAttribute("success", "Tạo ca làm việc thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/shifts";
    }

    @GetMapping("/admin/shifts/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String deleteShift(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            attendanceService.deleteShift(id);
            ra.addFlashAttribute("success", "Đã xóa ca làm việc.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/shifts";
    }

    private boolean matchesShiftPeriod(Shift shift, String period) {
        if (shift == null) {
            return false;
        }
        String normalized = period.toLowerCase();
        String name = shift.getName() != null ? shift.getName().toLowerCase() : "";
        LocalTime start = shift.getStartTime();

        if ("morning".equals(normalized)) {
            return name.contains("sang") || name.contains("sáng")
                    || (start != null && !start.isBefore(LocalTime.of(5, 0)) && start.isBefore(LocalTime.of(12, 0)));
        }
        if ("afternoon".equals(normalized)) {
            return name.contains("chieu") || name.contains("chiều")
                    || (start != null && !start.isBefore(LocalTime.of(12, 0)) && start.isBefore(LocalTime.of(17, 0)));
        }
        if ("evening".equals(normalized)) {
            return name.contains("toi") || name.contains("tối")
                    || (start != null && !start.isBefore(LocalTime.of(17, 0)) && start.isBefore(LocalTime.of(22, 0)));
        }
        if ("night".equals(normalized)) {
            return name.contains("dem") || name.contains("đêm")
                    || (start != null && (start.isBefore(LocalTime.of(5, 0)) || !start.isBefore(LocalTime.of(22, 0))));
        }
        return true;
    }

    private void applyPeriodDefaults(Shift shift, String period) {
        if (period == null || period.isBlank()) {
            return;
        }
        switch (period.toLowerCase()) {
            case "morning" -> {
                shift.setName("Ca sáng");
                shift.setStartTime(LocalTime.of(8, 0));
                shift.setEndTime(LocalTime.of(12, 0));
            }
            case "afternoon" -> {
                shift.setName("Ca chiều");
                shift.setStartTime(LocalTime.of(13, 0));
                shift.setEndTime(LocalTime.of(17, 0));
            }
            case "evening" -> {
                shift.setName("Ca tối");
                shift.setStartTime(LocalTime.of(18, 0));
                shift.setEndTime(LocalTime.of(22, 0));
            }
            case "night" -> {
                shift.setName("Ca đêm");
                shift.setStartTime(LocalTime.of(22, 0));
                shift.setEndTime(LocalTime.of(6, 0));
            }
            default -> {
                return;
            }
        }
        shift.setGracePeriodMinutes(10);
        shift.setOvertimeThresholdMinutes(30);
        shift.setIsActive(true);
    }
    
    // ===== Location Management (Admin) =====
    
    @GetMapping("/admin/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String locationList(Model model) {
        model.addAttribute("locations", attendanceService.getActiveLocations());
        return "admin/location-list";
    }
    
    @PostMapping("/admin/location/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String saveLocation(@RequestParam String name,
                              @RequestParam Double latitude,
                              @RequestParam Double longitude,
                              @RequestParam Integer radiusMeters,
                              @RequestParam(required = false) String address,
                              RedirectAttributes ra) {
        try {
            attendanceService.createLocation(name, latitude, longitude, radiusMeters, address);
            ra.addFlashAttribute("success", "Tạo địa điểm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/locations";
    }
    
    // ===== My Shift Schedule =====
    
    @GetMapping("/attendance/advanced/my-schedule")
    @PreAuthorize("isAuthenticated()")
    public String mySchedule(@RequestParam(required = false) String month,
                            Authentication auth,
                            Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        LocalDate now = LocalDate.now();
        LocalDate startDate = month != null ? LocalDate.parse(month + "-01") : now.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        var assignments = attendanceService.getUserShiftAssignments(user, startDate, endDate);
        
        model.addAttribute("assignments", assignments);
        model.addAttribute("currentMonth", startDate);
        
        return "attendance/my-schedule";
    }
}
