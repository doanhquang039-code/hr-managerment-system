package com.example.hr.kafka.events;


import com.example.hr.department.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event cho Employee Lifecycle (Vòng đời nhân viên)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLifecycleEvent {
    private Integer employeeId;
    private String username;
    private String fullName;
    private String eventType; // ONBOARDED, PROMOTED, TRANSFERRED, RESIGNED, TERMINATED
    private String department;
    private String position;
    private String newDepartment;
    private String newPosition;
    private LocalDate effectiveDate;
    private String reason;
    private LocalDateTime timestamp;
}
