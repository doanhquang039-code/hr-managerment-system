package com.example.hr.dto;

import com.example.hr.enums.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class WorkShiftDTO {

    @NotBlank(message = "TÃªn ca lÃ  báº¯t buá»™c")
    private String shiftName;

    @NotBlank(message = "MÃ£ ca lÃ  báº¯t buá»™c")
    private String shiftCode;

    private ShiftType shiftType = ShiftType.REGULAR;

    @NotNull(message = "Giá» báº¯t Ä‘áº§u lÃ  báº¯t buá»™c")
    private LocalTime startTime;

    @NotNull(message = "Giá» káº¿t thÃºc lÃ  báº¯t buá»™c")
    private LocalTime endTime;

    private Integer breakMinutes = 60;

    private BigDecimal allowance = BigDecimal.ZERO;

    private Boolean isActive = true;

    private String description;
}


