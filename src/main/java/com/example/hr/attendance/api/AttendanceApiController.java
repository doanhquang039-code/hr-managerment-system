package com.example.hr.attendance.api;



import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.entity.AttendanceLocation;
import com.example.hr.models.*;
import com.example.hr.attendance.service.AdvancedAttendanceService;
import com.example.hr.service.AuthUserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {
    
    private final AdvancedAttendanceService attendanceService;
    private final AuthUserHelper authUserHelper;
    
    @GetMapping("/locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getLocations() {
        List<AttendanceLocation> locations = attendanceService.getActiveLocations();
        return ResponseEntity.ok(locations);
    }
    
    @PostMapping("/geofencing-checkin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> geofencingCheckin(@RequestBody Map<String, Object> request,
                                               Authentication auth) {
        try {
            Double latitude = Double.parseDouble(request.get("latitude").toString());
            Double longitude = Double.parseDouble(request.get("longitude").toString());
            Integer locationId = Integer.parseInt(request.get("locationId").toString());
            String notes = request.getOrDefault("notes", "").toString();
            
            // Validate geofencing
            boolean isValid = attendanceService.validateGeofencing(latitude, longitude, locationId);
            
            if (!isValid) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Bạn không ở trong bán kính cho phép");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Create attendance record (simplified - in real app, save to DB)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chấm công thành công");
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            response.put("locationId", locationId);
            response.put("notes", notes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/face-recognition/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> registerFaceData(@RequestBody Map<String, Object> request,
                                              Authentication auth) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            String faceEncoding = request.get("faceEncoding").toString();
            String imageUrl = request.get("imageUrl").toString();
            
            FaceRecognitionData faceData = attendanceService.registerFaceData(user, faceEncoding, imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đăng ký nhận diện khuôn mặt thành công");
            response.put("faceData", faceData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/face-recognition/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFaceRecognitionStatus(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        boolean hasRecognition = attendanceService.hasFaceRecognition(user);
        Optional<FaceRecognitionData> faceData = attendanceService.getActiveFaceData(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasRecognition", hasRecognition);
        response.put("faceData", faceData.orElse(null));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-schedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMySchedule(@RequestParam String startDate,
                                          @RequestParam String endDate,
                                          Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        List<ShiftAssignment> assignments = attendanceService.getUserShiftAssignments(user, start, end);
        
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/shifts")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> getShifts() {
        List<Shift> shifts = attendanceService.getActiveShifts();
        return ResponseEntity.ok(shifts);
    }
    
    @PostMapping("/shift/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<?> assignShift(@RequestBody Map<String, Object> request,
                                        Authentication auth) {
        try {
            User assignedBy = authUserHelper.getCurrentUser(auth);
            
            // Parse request (simplified)
            Integer userId = Integer.parseInt(request.get("userId").toString());
            Integer shiftId = Integer.parseInt(request.get("shiftId").toString());
            LocalDate date = LocalDate.parse(request.get("date").toString());
            String notes = request.getOrDefault("notes", "").toString();
            
            // Get user and shift (simplified - in real app, fetch from DB)
            User user = new User();
            user.setId(userId);
            
            Shift shift = new Shift();
            shift.setId(shiftId);
            
            ShiftAssignment assignment = attendanceService.assignShift(user, shift, date, assignedBy, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Gán ca làm việc thành công");
            response.put("assignment", assignment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
