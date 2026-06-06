package com.example.hr.payroll.kafka.consumer;


import com.example.hr.payment.entity.Payment;
import com.example.hr.kafka.events.NotificationEvent;
import com.example.hr.kafka.events.PayrollEvent;
import com.example.hr.kafka.producer.HREventProducer;
import com.example.hr.enums.PaymentStatus;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.payroll.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Consumer cho Payroll Events
 * Xử lý các sự kiện bảng lương
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollEventConsumer {

    private final HREventProducer eventProducer;
    private final PayrollRepository payrollRepository;

    @KafkaListener(topics = "${kafka.topics.payroll}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePayrollEvent(PayrollEvent event) {
        try {
            log.info("Received payroll event: userId={}, eventType={}, month={}/{}", 
                    event.getUserId(), event.getEventType(), event.getMonth(), event.getYear());
            
            switch (event.getEventType()) {
                case "GENERATED":
                    handlePayrollGenerated(event);
                    break;
                case "APPROVED":
                    handlePayrollApproved(event);
                    break;
                case "PAID":
                    handlePayrollPaid(event);
                    break;
                case "REJECTED":
                    handlePayrollRejected(event);
                    break;
                default:
                    log.warn("Unknown payroll event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing payroll event: userId={}", event.getUserId(), e);
            throw new IllegalStateException("Failed to process payroll event", e);
        }
    }

    private void handlePayrollGenerated(PayrollEvent event) {
        log.info("Processing payroll generation: user={}, netSalary={}", 
                event.getFullName(), event.getNetSalary());
        Payroll payroll = findPayroll(event);
        payroll.setPaymentStatus(PaymentStatus.PENDING);
        payroll.setUpdatedAt(LocalDateTime.now());
        payrollRepository.save(payroll);
    }

    private void handlePayrollApproved(PayrollEvent event) {
        log.info("Processing payroll approval: user={}, netSalary={}", 
                event.getFullName(), event.getNetSalary());
        Payroll payroll = findPayroll(event);
        payroll.setPaymentStatus(PaymentStatus.PENDING);
        payroll.setUpdatedAt(LocalDateTime.now());
        payrollRepository.save(payroll);
    }

    private void handlePayrollPaid(PayrollEvent event) {
        log.info("Processing payroll payment: user={}, netSalary={}", 
                event.getFullName(), event.getNetSalary());

        Payroll payroll = findPayroll(event);
        payroll.setPaymentStatus(PaymentStatus.PAID);
        payroll.setUpdatedAt(LocalDateTime.now());
        payrollRepository.save(payroll);
        
        // Gửi notification cho employee
        NotificationEvent notification = new NotificationEvent(
                "EMAIL",
                Collections.singletonList(event.getUserId()),
                "Lương tháng " + event.getMonth() + "/" + event.getYear() + " đã được thanh toán",
                String.format("Lương tháng %d/%d của bạn đã được thanh toán. Số tiền: %s VND",
                        event.getMonth(), event.getYear(), event.getNetSalary()),
                "HIGH",
                "PAYROLL",
                event.getPayrollId(),
                "PAYROLL",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private void handlePayrollRejected(PayrollEvent event) {
        log.info("Processing payroll rejection: user={}", event.getFullName());
        Payroll payroll = findPayroll(event);
        payroll.setPaymentStatus(PaymentStatus.CANCELLED);
        payroll.setUpdatedAt(LocalDateTime.now());
        payrollRepository.save(payroll);

        NotificationEvent notification = new NotificationEvent(
                "IN_APP",
                Collections.singletonList(event.getUserId()),
                "Bang luong can xu ly lai",
                String.format("Bang luong thang %d/%d cua %s da bi tu choi/can tinh lai",
                        event.getMonth(), event.getYear(), event.getFullName()),
                "HIGH",
                "PAYROLL",
                event.getPayrollId(),
                "PAYROLL",
                LocalDateTime.now()
        );
        eventProducer.publishNotificationEvent(notification);
    }

    private Payroll findPayroll(PayrollEvent event) {
        if (event.getPayrollId() != null) {
            return payrollRepository.findById(event.getPayrollId())
                    .orElseThrow(() -> new IllegalArgumentException("Payroll not found: " + event.getPayrollId()));
        }
        return payrollRepository.findByUserIdAndMonthAndYear(event.getUserId(), event.getMonth(), event.getYear())
                .orElseThrow(() -> new IllegalArgumentException("Payroll not found for user/month/year"));
    }
}
