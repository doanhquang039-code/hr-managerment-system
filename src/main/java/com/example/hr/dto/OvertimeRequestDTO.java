package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeRequestDTO {

    private Integer id;

    @NotNull(message = "User ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer userId;

    @NotNull(message = "NgÃ y OT khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate overtimeDate;

    @NotNull(message = "Giá» báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime startTime;

    @NotNull(message = "Giá» káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalTime endTime;

    private BigDecimal totalHours;
    private BigDecimal multiplier;
    private String reason;

    // DÃ¹ng cho approval/rejection
    private Integer approvedById;
    private String rejectionReason;
}


