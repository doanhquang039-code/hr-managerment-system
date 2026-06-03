package com.example.hr.kafka.producer;

import com.example.hr.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer cho tất cả HR Events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HREventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.attendance}")
    private String attendanceTopic;

    @Value("${kafka.topics.leave-requests}")
    private String leaveRequestsTopic;

    @Value("${kafka.topics.payroll}")
    private String payrollTopic;

    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;

    @Value("${kafka.topics.performance-reviews}")
    private String performanceReviewsTopic;

    @Value("${kafka.topics.recruitment}")
    private String recruitmentTopic;

    @Value("${kafka.topics.training}")
    private String trainingTopic;

    @Value("${kafka.topics.employee-lifecycle}")
    private String employeeLifecycleTopic;

    @Value("${kafka.topics.audit-events}")
    private String auditEventsTopic;

    @Value("${kafka.topics.health-insights}")
    private String healthInsightsTopic;

    /**
     * Publish Attendance Event
     */
    public void publishAttendanceEvent(AttendanceEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(attendanceTopic, event.getUserId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Attendance event published: userId={}, eventType={}", 
                            event.getUserId(), event.getEventType());
                } else {
                    log.error("Failed to publish attendance event: userId={}", 
                            event.getUserId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing attendance event", e);
        }
    }

    /**
     * Publish Leave Request Event
     */
    public void publishLeaveRequestEvent(LeaveRequestEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(leaveRequestsTopic, event.getUserId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Leave request event published: userId={}, eventType={}", 
                            event.getUserId(), event.getEventType());
                } else {
                    log.error("Failed to publish leave request event: userId={}", 
                            event.getUserId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing leave request event", e);
        }
    }

    /**
     * Publish Payroll Event
     */
    public void publishPayrollEvent(PayrollEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(payrollTopic, event.getUserId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Payroll event published: userId={}, eventType={}", 
                            event.getUserId(), event.getEventType());
                } else {
                    log.error("Failed to publish payroll event: userId={}", 
                            event.getUserId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing payroll event", e);
        }
    }

    /**
     * Publish Notification Event
     */
    public void publishNotificationEvent(NotificationEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(notificationsTopic, event.getCategory(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Notification event published: type={}, category={}", 
                            event.getNotificationType(), event.getCategory());
                } else {
                    log.error("Failed to publish notification event: category={}", 
                            event.getCategory(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing notification event", e);
        }
    }

    /**
     * Publish Performance Review Event
     */
    public void publishPerformanceReviewEvent(PerformanceReviewEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(performanceReviewsTopic, event.getEmployeeId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Performance review event published: employeeId={}, eventType={}", 
                            event.getEmployeeId(), event.getEventType());
                } else {
                    log.error("Failed to publish performance review event: employeeId={}", 
                            event.getEmployeeId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing performance review event", e);
        }
    }

    /**
     * Publish Recruitment Event
     */
    public void publishRecruitmentEvent(RecruitmentEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(recruitmentTopic, event.getApplicationId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Recruitment event published: applicationId={}, eventType={}", 
                            event.getApplicationId(), event.getEventType());
                } else {
                    log.error("Failed to publish recruitment event: applicationId={}", 
                            event.getApplicationId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing recruitment event", e);
        }
    }

    /**
     * Publish Training Event
     */
    public void publishTrainingEvent(TrainingEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(trainingTopic, event.getEmployeeId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Training event published: employeeId={}, eventType={}", 
                            event.getEmployeeId(), event.getEventType());
                } else {
                    log.error("Failed to publish training event: employeeId={}", 
                            event.getEmployeeId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing training event", e);
        }
    }

    /**
     * Publish Employee Lifecycle Event
     */
    public void publishEmployeeLifecycleEvent(EmployeeLifecycleEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(employeeLifecycleTopic, event.getEmployeeId().toString(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Employee lifecycle event published: employeeId={}, eventType={}", 
                            event.getEmployeeId(), event.getEventType());
                } else {
                    log.error("Failed to publish employee lifecycle event: employeeId={}", 
                            event.getEmployeeId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing employee lifecycle event", e);
        }
    }

    public void publishAuditEvent(AuditEvent event) {
        try {
            String key = event.getEntityType() != null ? event.getEntityType() : "AUDIT";
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(auditEventsTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Audit event published: actor={}, action={}, entityType={}",
                            event.getActorUsername(), event.getAction(), event.getEntityType());
                } else {
                    log.error("Failed to publish audit event: actor={}, action={}",
                            event.getActorUsername(), event.getAction(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing audit event", e);
            throw e;
        }
    }

    public void publishHealthInsightEvent(HealthInsightEvent event) {
        try {
            String key = event.getUserId() != null ? event.getUserId().toString() : "anonymous";
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(healthInsightsTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Health insight event published: userId={}, riskLevel={}, score={}",
                            event.getUserId(), event.getRiskLevel(), event.getWellnessScore());
                } else {
                    log.error("Failed to publish health insight event: userId={}", event.getUserId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing health insight event", e);
            throw e;
        }
    }
}
