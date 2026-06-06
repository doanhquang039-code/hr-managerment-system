package com.example.hr.service;

import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.NotificationType;
import com.example.hr.enums.OvertimeStatus;
import com.example.hr.exception.ApprovalWorkflowException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.models.OvertimeRequest;
import com.example.hr.models.User;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.repository.OvertimeRequestRepository;
import com.example.hr.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service generic approval workflow.
 * Xử lý multi-level approval cho Leave, OT, và các request khác.
 */
@Service
@Transactional
public class WorkflowApprovalService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowApprovalService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRequestRepository overtimeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final HrAuditLogService auditLogService;
    private final NewOvertimeService newOvertimeService;

    public WorkflowApprovalService(LeaveRequestRepository leaveRequestRepository,
                                     OvertimeRequestRepository overtimeRepository,
                                     UserRepository userRepository,
                                     NotificationService notificationService,
                                     HrAuditLogService auditLogService,
                                     NewOvertimeService newOvertimeService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.overtimeRepository = overtimeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.newOvertimeService = newOvertimeService;
    }

    // ===================== LEAVE APPROVAL =====================

    /**
     * Duyệt nghỉ phép.
     */
    public LeaveRequest approveLeave(Integer leaveId, Integer approverId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn nghỉ phép", leaveId));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Người duyệt", approverId));

        validateLeaveApproval(leave, approver);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        // Notify employee
        sendApprovalNotification(leave.getUser(), "Đơn nghỉ phép", true, null);

        // Audit log
        auditLogService.log(approver.getUsername(), "APPROVE_LEAVE",
                "LeaveRequest", String.valueOf(leaveId),
                "Đã duyệt nghỉ phép cho " + leave.getUser().getFullName(), "N/A");

        log.info("Leave approved: id={}, user={}, approver={}",
                leaveId, leave.getUser().getUsername(), approver.getUsername());

        return saved;
    }

    /**
     * Từ chối nghỉ phép.
     */
    public LeaveRequest rejectLeave(Integer leaveId, Integer approverId, String reason) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn nghỉ phép", leaveId));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Người duyệt", approverId));

        validateLeaveApproval(leave, approver);

        if (reason == null || reason.isBlank()) {
            throw new ApprovalWorkflowException("Phải cung cấp lý do từ chối");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        // Notify employee
        sendApprovalNotification(leave.getUser(), "Đơn nghỉ phép", false, reason);

        // Audit log
        auditLogService.log(approver.getUsername(), "REJECT_LEAVE",
                "LeaveRequest", String.valueOf(leaveId),
                "Đã từ chối nghỉ phép: " + reason, "N/A");

        return saved;
    }

    /**
     * Duyệt hàng loạt nghỉ phép.
     */
    public int batchApproveLeaves(List<Integer> leaveIds, Integer approverId) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Người duyệt", approverId));
        List<LeaveRequest> requests = leaveRequestRepository.findAllById(leaveIds);
        List<LeaveRequest> approvedList = new java.util.ArrayList<>();
        
        for (LeaveRequest leave : requests) {
            try {
                validateLeaveApproval(leave, approver);
                leave.setStatus(LeaveStatus.APPROVED);
                leave.setApprovedBy(approver);
                approvedList.add(leave);
                
                sendApprovalNotification(leave.getUser(), "Đơn nghỉ phép", true, null);
                auditLogService.log(approver.getUsername(), "APPROVE_LEAVE",
                    "LeaveRequest", String.valueOf(leave.getId()),
                    "Đã duyệt nghỉ phép cho " + leave.getUser().getFullName(), "N/A");
            } catch (Exception e) {
                log.error("Failed to approve leave {}: {}", leave.getId(), e.getMessage());
            }
        }
        if (!approvedList.isEmpty()) {
            leaveRequestRepository.saveAll(approvedList);
        }
        return approvedList.size();
    }

    // ===================== OVERTIME APPROVAL =====================

    /**
     * Duyệt OT.
     */
    public OvertimeRequest approveOvertime(Integer overtimeId, Integer approverId) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Người duyệt", approverId));
        return newOvertimeService.approveRequest(overtimeId, approver);
    }

    /**
     * Từ chối OT.
     */
    public OvertimeRequest rejectOvertime(Integer overtimeId, Integer approverId, String reason) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Người duyệt", approverId));
        return newOvertimeService.rejectRequest(overtimeId, approver, reason);
    }

    /**
     * Duyệt hàng loạt OT.
     */
    public int batchApproveOvertime(List<Integer> overtimeIds, Integer approverId) {
        int count = 0;
        for (Integer id : overtimeIds) {
            try {
                approveOvertime(id, approverId);
                count++;
            } catch (Exception e) {
                log.error("Failed to approve overtime {}: {}", id, e.getMessage());
            }
        }
        return count;
    }

    // ===================== STATISTICS =====================

    /**
     * Thống kê pending approvals.
     */
    @Transactional(readOnly = true)
    public PendingApprovalStats getPendingStats() {
        long pendingLeaves = leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.PENDING)
                .count();
        long pendingOT = overtimeRepository.countByStatus(OvertimeStatus.PENDING.name());

        return new PendingApprovalStats(pendingLeaves, pendingOT);
    }

    // --- Helper classes ---

    public record PendingApprovalStats(long pendingLeaves, long pendingOvertime) {
        public long total() {
            return pendingLeaves + pendingOvertime;
        }
    }

    // --- Private helpers ---

    private void validateLeaveApproval(LeaveRequest leave, User approver) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ApprovalWorkflowException("Chỉ có thể xử lý đơn đang Chờ duyệt. Trạng thái hiện tại: " + leave.getStatus());
        }
        if (leave.getUser().getId().equals(approver.getId())) {
            throw new ApprovalWorkflowException("Không thể tự duyệt đơn nghỉ phép của mình");
        }
    }

    private void validateOvertimeApproval(OvertimeRequest ot, User approver) {
        if (!ot.getStatus().equals(OvertimeStatus.PENDING.name())) {
            throw new ApprovalWorkflowException("Chỉ có thể xử lý đơn đang Chờ duyệt. Trạng thái hiện tại: " + ot.getStatus());
        }
        if (ot.getUser().getId().equals(approver.getId())) {
            throw new ApprovalWorkflowException("Không thể tự duyệt đơn OT của mình");
        }
    }

    private void sendApprovalNotification(User employee, String requestType, boolean approved, String reason) {
        try {
            String status = approved ? "đã được duyệt" : "đã bị từ chối";
            String message = requestType + " của bạn " + status;
            if (reason != null) {
                message += ". Lý do: " + reason;
            }
            notificationService.createNotification(employee, message, NotificationType.INFO, "/user1/dashboard");
        } catch (Exception e) {
            log.error("Failed to send approval notification: {}", e.getMessage());
        }
    }
}
