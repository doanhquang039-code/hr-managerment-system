package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftAssignmentDTO {

    @NotNull(message = "userId là bắt buộc")
    private Integer userId;

    @NotNull(message = "shiftId là bắt buộc")
    private Integer shiftId;

    @NotNull(message = "Ngày làm việc là bắt buộc")
    private LocalDate workDate;

    private String note;
}
