package com.example.hr.service;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.AttendanceStatus;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.NotificationType;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.models.PerformanceReview;
import com.example.hr.models.User;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.repository.PerformanceReviewRepository;
import com.example.hr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttritionRiskService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> calculateRisks() {
        return userRepository.findByStatus(UserStatus.ACTIVE).stream()
                .filter(user -> user.getRole() == Role.USER)
                .map(this::scoreUser)
                .sorted(Comparator.comparing((Map<String, Object> item) -> (Integer) item.get("score")).reversed())
                .toList();
    }

    @Scheduled(cron = "${hr.attrition-risk.cron:0 30 8 * * MON}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void notifyHighRiskEmployees() {
        List<Map<String, Object>> highRisk = calculateRisks().stream()
                .filter(item -> (Integer) item.get("score") >= 70)
                .toList();
        if (highRisk.isEmpty()) {
            return;
        }
        String message = "CÃ³ " + highRisk.size() + " nhÃ¢n sá»± cÃ³ nguy cÆ¡ nghá»‰ viá»‡c cao. Xem API /api/hr-intelligence/attrition-risk.";
        for (User approver : userRepository.findByRoleInAndStatus(List.of(Role.ADMIN, Role.MANAGER, Role.HIRING), UserStatus.ACTIVE)) {
            notificationService.createNotification(approver, message, NotificationType.WARNING, "/admin/dashboard");
        }
    }

    private Map<String, Object> scoreUser(User user) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(90);
        List<Attendance> attendance = attendanceRepository.findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(user, start, today);
        List<LeaveRequest> leaves = leaveRequestRepository.findByUser(user);
        PerformanceReview latestReview = performanceReviewRepository.findTopByEmployeeOrderByReviewDateDesc(user).orElse(null);

        int score = 0;
        Map<String, Object> reasons = new LinkedHashMap<>();

        long absentOrLate = attendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT || a.getStatus() == AttendanceStatus.LATE || a.getStatus() == AttendanceStatus.EARLY_LEAVE)
                .count();
        if (absentOrLate >= 8) {
            score += 25;
            reasons.put("attendance", absentOrLate + " láº§n váº¯ng/muá»™n/vá» sá»›m trong 90 ngÃ y");
        } else if (absentOrLate >= 4) {
            score += 12;
            reasons.put("attendance", absentOrLate + " láº§n váº¯ng/muá»™n/vá» sá»›m trong 90 ngÃ y");
        }

        long recentLeaves = leaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .filter(l -> l.getStartDate() != null && !l.getStartDate().isBefore(start))
                .count();
        if (recentLeaves >= 4) {
            score += 20;
            reasons.put("leave", recentLeaves + " Ä‘Æ¡n nghá»‰ Ä‘Ã£ duyá»‡t trong 90 ngÃ y");
        }

        if (latestReview == null) {
            score += 10;
            reasons.put("review", "ChÆ°a cÃ³ Ä‘Ã¡nh giÃ¡ hiá»‡u suáº¥t gáº§n nháº¥t");
        } else if (latestReview.getOverallScore() != null && latestReview.getOverallScore() <= 2) {
            score += 25;
            reasons.put("review", "Äiá»ƒm hiá»‡u suáº¥t tháº¥p: " + latestReview.getOverallScore());
        } else if (latestReview.getReviewDate() != null && ChronoUnit.DAYS.between(latestReview.getReviewDate(), today) > 180) {
            score += 8;
            reasons.put("review", "ÄÃ¡nh giÃ¡ hiá»‡u suáº¥t Ä‘Ã£ quÃ¡ 180 ngÃ y");
        }

        if (user.getHireDate() != null) {
            long tenureDays = ChronoUnit.DAYS.between(user.getHireDate(), today);
            if (tenureDays < 180) {
                score += 10;
                reasons.put("tenure", "NhÃ¢n sá»± má»›i dÆ°á»›i 6 thÃ¡ng");
            }
        }

        String riskLevel = score >= 70 ? "HIGH" : score >= 40 ? "MEDIUM" : "LOW";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employeeId", user.getId());
        result.put("employeeCode", user.getEmployeeCode());
        result.put("fullName", user.getFullName());
        result.put("department", user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null);
        result.put("score", Math.min(score, 100));
        result.put("riskLevel", riskLevel);
        result.put("reasons", reasons);
        return result;
    }
}


