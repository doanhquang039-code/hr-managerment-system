package com.example.hr.service;

import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.LeaveType;
import com.example.hr.exception.BusinessValidationException;
import com.example.hr.models.LeaveRequest;
import com.example.hr.repository.LeaveRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service quản lý số ngày nghỉ phép còn lại.
 * Bao gồm: accrual logic, carry-over, validation.
 */
@Service
@Transactional
public class LeaveBalanceService {

    private static final Logger log = LoggerFactory.getLogger(LeaveBalanceService.class);

    // Số ngày phép mặc định mỗi năm theo loại
    private static final Map<LeaveType, Integer> ANNUAL_ENTITLEMENTS = Map.of(
            LeaveType.ANNUAL, 12,       // Phép năm: 12 ngày
            LeaveType.SICK, 30,          // Ốm: 30 ngày
            LeaveType.MATERNITY, 180,    // Thai sản: 180 ngày
            LeaveType.PATERNITY, 7,      // Chăm con: 7 ngày (tuỳ chính sách)
            LeaveType.MARRIAGE, 3,       // Nghỉ cưới: 3 ngày
            LeaveType.BEREAVEMENT, 3     // Tang: 3 ngày
    );

    // Tối đa carry-over từ năm trước
    private static final int MAX_CARRY_OVER = 5;

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveBalanceService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    /**
     * Tính số ngày nghỉ cho một leave request.
     */
    public long calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Math.max(0, days);
    }

    /**
     * Lấy số ngày phép đã sử dụng theo loại trong năm.
     */
    @Transactional(readOnly = true)
    public long getUsedDays(Integer userId, LeaveType leaveType, int year) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getUser().getId().equals(userId)
                        && lr.getLeaveType() == leaveType
                        && lr.getStatus() == LeaveStatus.APPROVED
                        && lr.getStartDate().getYear() == year)
                .toList();

        long totalDays = 0;
        for (LeaveRequest leave : approvedLeaves) {
            totalDays += calculateLeaveDays(leave.getStartDate(), leave.getEndDate());
        }
        return totalDays;
    }

    /**
     * Lấy entitlement cho loại nghỉ phép.
     */
    public int getEntitlement(LeaveType leaveType) {
        return ANNUAL_ENTITLEMENTS.getOrDefault(leaveType, 0);
    }

    /**
     * Tính số ngày còn lại cho loại nghỉ phép.
     */
    @Transactional(readOnly = true)
    public long getRemainingDays(Integer userId, LeaveType leaveType, int year) {
        int entitlement = getEntitlement(leaveType);
        long used = getUsedDays(userId, leaveType, year);
        return Math.max(0, entitlement - used);
    }

    /**
     * Lấy balance tổng hợp cho nhân viên.
     */
    @Transactional(readOnly = true)
    public Map<LeaveType, Map<String, Long>> getLeaveBalance(Integer userId, int year) {
        Map<LeaveType, Map<String, Long>> balance = new HashMap<>();

        for (LeaveType type : LeaveType.values()) {
            int entitlement = getEntitlement(type);
            if (entitlement == 0) continue;

            long used = getUsedDays(userId, type, year);
            long remaining = Math.max(0, entitlement - used);

            Map<String, Long> typeBalance = new HashMap<>();
            typeBalance.put("entitlement", (long) entitlement);
            typeBalance.put("used", used);
            typeBalance.put("remaining", remaining);

            balance.put(type, typeBalance);
        }

        return balance;
    }

    /**
     * Validate yêu cầu nghỉ phép có đủ ngày còn lại không.
     */
    public void validateLeaveRequest(Integer userId, LeaveType leaveType,
                                       LocalDate startDate, LocalDate endDate) {
        long requestedDays = calculateLeaveDays(startDate, endDate);
        long remainingDays = getRemainingDays(userId, leaveType, startDate.getYear());

        if (requestedDays > remainingDays) {
            throw new BusinessValidationException(
                    String.format("Không đủ ngày phép %s. Yêu cầu: %d ngày, Còn lại: %d ngày",
                            leaveType.name(), requestedDays, remainingDays));
        }

        // Validate ngày bắt đầu không trong quá khứ
        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessValidationException("Ngày bắt đầu không được trong quá khứ");
        }

        // Validate end >= start
        if (endDate.isBefore(startDate)) {
            throw new BusinessValidationException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    /**
     * Tính carry-over từ năm trước.
     */
    @Transactional(readOnly = true)
    public long calculateCarryOver(Integer userId, int currentYear) {
        int previousYear = currentYear - 1;
        long remainingLastYear = getRemainingDays(userId, LeaveType.ANNUAL, previousYear);
        return Math.min(remainingLastYear, MAX_CARRY_OVER);
    }

    /**
     * Lấy thống kê nghỉ phép theo phòng ban.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getLeaveCountByDepartment(int year) {
        Map<String, Long> result = new HashMap<>();
        List<LeaveRequest> allLeaves = leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED
                        && lr.getStartDate().getYear() == year
                        && lr.getUser().getDepartment() != null)
                .toList();

        for (LeaveRequest leave : allLeaves) {
            String dept = leave.getUser().getDepartment().getDepartmentName();
            long days = calculateLeaveDays(leave.getStartDate(), leave.getEndDate());
            result.merge(dept, days, Long::sum);
        }

        return result;
    }

    /**
     * Kiểm tra nhân viên có đang nghỉ phép hôm nay không.
     */
    @Transactional(readOnly = true)
    public boolean isOnLeaveToday(Integer userId) {
        LocalDate today = LocalDate.now();
        return leaveRequestRepository.findAll().stream()
                .anyMatch(lr -> lr.getUser().getId().equals(userId)
                        && lr.getStatus() == LeaveStatus.APPROVED
                        && !today.isBefore(lr.getStartDate())
                        && !today.isAfter(lr.getEndDate()));
    }

    /**
     * Đếm nhân viên đang nghỉ hôm nay.
     */
    @Transactional(readOnly = true)
    public long countEmployeesOnLeaveToday() {
        LocalDate today = LocalDate.now();
        return leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED
                        && !today.isBefore(lr.getStartDate())
                        && !today.isAfter(lr.getEndDate()))
                .map(lr -> lr.getUser().getId())
                .distinct()
                .count();
    }
}
