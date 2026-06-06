package com.example.hr.leave.dto;

import com.example.hr.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    
    private Long id;
    private LeaveType leaveType;
    private String leaveTypeName;
    private Integer year;
    private Double totalDays;
    private Double usedDays;
    private Double remainingDays;
    private Double carriedForward;
    private Double pendingDays;
}
