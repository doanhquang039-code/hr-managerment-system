package com.example.hr.attendance.service;


import com.example.hr.payroll.entity.Payroll;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.entity.AttendanceLocation;
import com.example.hr.attendance.repository.AttendanceLocationRepository;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdvancedAttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final AttendanceLocationRepository locationRepository;
    private final FaceRecognitionDataRepository faceRecognitionRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    
    /**
     * Validate geofencing - check if user is within allowed location
     */
    public boolean validateGeofencing(Double userLat, Double userLng, Integer locationId) {
        Optional<AttendanceLocation> locationOpt = locationRepository.findById(locationId);
        if (locationOpt.isEmpty() || !locationOpt.get().getIsActive()) {
            return false;
        }
        
        AttendanceLocation location = locationOpt.get();
        double distance = calculateDistance(userLat, userLng, 
                                           location.getLatitude(), location.getLongitude());
        
        return distance <= location.getRadiusMeters();
    }
    
    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Get all active locations
     */
    @Transactional(readOnly = true)
    public List<AttendanceLocation> getActiveLocations() {
        return locationRepository.findByIsActiveTrue();
    }
    
    /**
     * Create attendance location
     */
    public AttendanceLocation createLocation(String name, Double latitude, Double longitude, 
                                            Integer radiusMeters, String address) {
        AttendanceLocation location = new AttendanceLocation();
        location.setName(name);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setRadiusMeters(radiusMeters);
        location.setAddress(address);
        location.setIsActive(true);
        
        return locationRepository.save(location);
    }
    
    /**
     * Register face recognition data for user
     */
    public FaceRecognitionData registerFaceData(User user, String faceEncoding, String imageUrl) {
        // Deactivate old face data
        List<FaceRecognitionData> oldData = faceRecognitionRepository.findByUserAndIsActiveTrue(user);
        oldData.forEach(data -> data.setIsActive(false));
        if (!oldData.isEmpty()) {
            faceRecognitionRepository.saveAll(oldData);
        }
        
        // Create new face data
        FaceRecognitionData faceData = new FaceRecognitionData();
        faceData.setUser(user);
        faceData.setFaceEncoding(faceEncoding);
        faceData.setImageUrl(imageUrl);
        faceData.setIsActive(true);
        
        return faceRecognitionRepository.save(faceData);
    }
    
    /**
     * Get active face data for user
     */
    @Transactional(readOnly = true)
    public Optional<FaceRecognitionData> getActiveFaceData(User user) {
        return faceRecognitionRepository.findFirstByUserAndIsActiveTrueOrderByCreatedAtDesc(user);
    }
    
    /**
     * Check if user has face recognition enabled
     */
    @Transactional(readOnly = true)
    public boolean hasFaceRecognition(User user) {
        return faceRecognitionRepository.existsByUserAndIsActiveTrue(user);
    }
    
    // ===== Shift Management =====
    
    /**
     * Create shift
     */
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Transactional(readOnly = true)
    public Shift getShiftById(Integer id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + id));
    }

    public void deleteShift(Integer id) {
        Shift shift = getShiftById(id);
        shift.setIsActive(false);
        shiftRepository.save(shift);
    }
    
    /**
     * Get all active shifts
     */
    @Transactional(readOnly = true)
    public List<Shift> getActiveShifts() {
        return shiftRepository.findByIsActiveTrue();
    }
    
    /**
     * Assign shift to user
     */
    public ShiftAssignment assignShift(User user, Shift shift, LocalDate date, User assignedBy, String notes) {
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setUser(user);
        assignment.setShift(shift);
        assignment.setAssignedDate(date);
        assignment.setAssignedBy(assignedBy);
        assignment.setNotes(notes);
        
        return shiftAssignmentRepository.save(assignment);
    }
    
    /**
     * Get shift assignment for user on specific date
     */
    @Transactional(readOnly = true)
    public Optional<ShiftAssignment> getShiftAssignment(User user, LocalDate date) {
        return shiftAssignmentRepository.findByUserAndAssignedDate(user, date);
    }
    
    /**
     * Get user's shift assignments in date range
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignment> getUserShiftAssignments(User user, LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findByUserAndAssignedDateBetween(user, startDate, endDate);
    }
    
    /**
     * Get all assignments for a specific date
     */
    @Transactional(readOnly = true)
    public List<ShiftAssignment> getAssignmentsByDate(LocalDate date) {
        return shiftAssignmentRepository.findByAssignedDate(date);
    }

    // ===== Monthly Closing =====
    
    /**
     * T�nh t?ng c�ng trong th�ng cho 1 nh�n vi�n
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> calculateMonthlySummary(User user, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(user, startDate, endDate);
            
        long totalDaysPresent = attendances.stream()
            .map(a -> a.getAttendanceDate())
            .distinct()
            .count();
            
        double totalWorkHours = 0;
        for (Attendance a : attendances) {
            if (a.getCheckOutTime() != null && a.getCheckInTime() != null) {
                totalWorkHours += java.time.Duration.between(a.getCheckInTime(), a.getCheckOutTime()).toMinutes() / 60.0;
            }
        }
        
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("year", year);
        summary.put("month", month);
        summary.put("totalDaysPresent", totalDaysPresent);
        summary.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        summary.put("attendanceCount", attendances.size());
        
        return summary;
    }
    
    /**
     * Ch?t c�ng h�ng th�ng cho to�n b? nh�n vi�n (Batch)
     */
    public List<java.util.Map<String, Object>> closeMonthlyAttendance(int year, int month, List<User> activeUsers) {
        List<java.util.Map<String, Object>> summaries = new java.util.ArrayList<>();
        for (User user : activeUsers) {
            summaries.add(calculateMonthlySummary(user, year, month));
        }
        // Thu?ng ? d�y s? luu v�o DB (b?ng MonthlyAttendanceSummary ho?c Payroll)
        return summaries;
    }
}
