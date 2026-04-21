package com.example.hr.dto;

import com.example.hr.enums.ShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class WorkShiftDTO {

    @NotBlank(message = "Tên ca là bắt buộc")
    private String shiftName;

    @NotBlank(message = "Mã ca là bắt buộc")
    private String shiftCode;

    private ShiftType shiftType = ShiftType.REGULAR;

    @NotNull(message = "Giờ bắt đầu là bắt buộc")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc là bắt buộc")
    private LocalTime endTime;

    private Integer breakMinutes = 60;

    private BigDecimal allowance = BigDecimal.ZERO;

    private Boolean isActive = true;

    private String description;
}
