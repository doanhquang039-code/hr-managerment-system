package com.example.hr.service;

import com.example.hr.models.OvertimeRequest;
import com.example.hr.models.User;
import com.example.hr.repository.OvertimeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NewOvertimeService {
    
    private final OvertimeRequestRepository overtimeRepository;
    
    /**
     * Create overtime request
     */
    public OvertimeRequest createRequest(User user, LocalDate overtimeDate, Double hours, String reason) {
        OvertimeRequest request = new OvertimeRequest();
        request.setUser(user);
        request.setOvertimeDate(overtimeDate);
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(18, 0).plusMinutes(Math.round(hours * 60)));
        request.setHours(hours);
        request.setReason(reason);
        request.setStatus("PENDING");
        request.setMultiplier(1.5); // Default multiplier
        
        return overtimeRepository.save(request);
    }

    public OvertimeRequest createRequest(User user, LocalDate overtimeDate, LocalTime startTime, LocalTime endTime, String reason) {
        double totalHours = calculateHours(startTime, endTime);
        if (totalHours <= 0) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (totalHours > 4) {
            throw new IllegalArgumentException("Overtime cannot exceed 4 hours per day");
        }

        OvertimeRequest request = new OvertimeRequest();
        request.setUser(user);
        request.setOvertimeDate(overtimeDate);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setTotalHours(totalHours);
        request.setReason(reason);
        request.setStatus("PENDING");
        request.setMultiplier(resolveMultiplier(overtimeDate));

        return overtimeRepository.save(request);
    }
    
    /**
     * Get overtime requests by user
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequest> getUserRequests(User user) {
        return overtimeRepository.findByUser(user);
    }
    
    /**
     * Get pending requests
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequest> getPendingRequests() {
        return overtimeRepository.findByStatus("PENDING");
    }
    
    /**
     * Approve overtime request
     */
    public OvertimeRequest approveRequest(Integer requestId, User approver) {
        Optional<OvertimeRequest> requestOpt = overtimeRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new RuntimeException("Overtime request not found");
        }
        
        OvertimeRequest request = requestOpt.get();
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is not pending");
        }
        
        request.setStatus("APPROVED");
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        
        return overtimeRepository.save(request);
    }
    
    /**
     * Reject overtime request
     */
    public OvertimeRequest rejectRequest(Integer requestId, User approver, String reason) {
        Optional<OvertimeRequest> requestOpt = overtimeRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new RuntimeException("Overtime request not found");
        }
        
        OvertimeRequest request = requestOpt.get();
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is not pending");
        }
        
        request.setStatus("REJECTED");
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setRejectionReason(reason);
        
        return overtimeRepository.save(request);
    }
    
    /**
     * Get total approved overtime hours for user in date range
     */
    @Transactional(readOnly = true)
    public Double getTotalApprovedHours(User user, LocalDate startDate, LocalDate endDate) {
        Double total = overtimeRepository.getTotalApprovedHours(user, startDate, endDate);
        return total != null ? total : 0.0;
    }
    
    /**
     * Get overtime requests in date range
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequest> getRequestsInDateRange(LocalDate startDate, LocalDate endDate) {
        return overtimeRepository.findByOvertimeDateBetween(startDate, endDate);
    }

    private double calculateHours(LocalTime startTime, LocalTime endTime) {
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (minutes <= 0) {
            minutes += 24 * 60;
        }
        return minutes / 60.0;
    }

    private double resolveMultiplier(LocalDate overtimeDate) {
        var day = overtimeDate.getDayOfWeek();
        return (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) ? 2.0 : 1.5;
    }
}


