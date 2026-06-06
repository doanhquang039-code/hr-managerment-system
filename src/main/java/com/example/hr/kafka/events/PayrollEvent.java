package com.example.hr.kafka.events;


import com.example.hr.payroll.entity.Payroll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event cho Payroll (Bảng lương)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEvent {
    private Integer payrollId;
    private Integer userId;
    private String username;
    private String fullName;
    private String eventType; // GENERATED, APPROVED, PAID, REJECTED
    private Integer month;
    private Integer year;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal bonuses;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private String status;
    private LocalDateTime timestamp;
}
