package com.example.hr.event;


import com.example.hr.leave.entity.LeaveRequest;
import com.example.hr.service.HrAuditLogService;
import com.example.hr.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener xử lý các HR domain events.
 * Async processing để không block main thread.
 */
@Component
public class HrEventListener {

    private static final Logger log = LoggerFactory.getLogger(HrEventListener.class);

    private final HrAuditLogService auditLogService;

    public HrEventListener(HrAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Async
    @EventListener
    public void handleEmployeeHired(EmployeeHiredEvent event) {
        log.info("Event: Employee hired - {} by {}", event.getEmployeeName(), event.getActorUsername());
        auditLogService.log(event.getActorUsername(), "HIRE_EMPLOYEE",
                "User", String.valueOf(event.getEmployeeId()),
                "Đã tuyển dụng nhân viên: " + event.getEmployeeName());
    }

    @Async
    @EventListener
    public void handleLeaveApproved(LeaveApprovedEvent event) {
        log.info("Event: Leave approved - leaveId={} by {}", event.getLeaveId(), event.getActorUsername());
        auditLogService.log(event.getActorUsername(), "APPROVE_LEAVE",
                "LeaveRequest", String.valueOf(event.getLeaveId()),
                "Đã duyệt nghỉ phép");
    }

    @Async
    @EventListener
    public void handleOvertimeApproved(OvertimeApprovedEvent event) {
        log.info("Event: OT approved - otId={} by {}", event.getOvertimeId(), event.getActorUsername());
        auditLogService.log(event.getActorUsername(), "APPROVE_OVERTIME",
                "OvertimeRequest", String.valueOf(event.getOvertimeId()),
                "Đã duyệt OT");
    }

    @Async
    @EventListener
    public void handleWarningIssued(WarningIssuedEvent event) {
        log.warn("Event: Warning issued - level={}, employeeId={} by {}",
                event.getLevel(), event.getEmployeeId(), event.getActorUsername());
        auditLogService.log(event.getActorUsername(), "ISSUE_WARNING",
                "EmployeeWarning", String.valueOf(event.getWarningId()),
                "Cảnh cáo mức " + event.getLevel().name());
    }
}
