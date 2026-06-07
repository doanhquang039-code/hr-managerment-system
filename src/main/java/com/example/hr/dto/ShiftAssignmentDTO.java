package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftAssignmentDTO {

    @NotNull(message = "userId lÃ  báº¯t buá»™c")
    private Integer userId;

    @NotNull(message = "shiftId lÃ  báº¯t buá»™c")
    private Integer shiftId;

    @NotNull(message = "NgÃ y lÃ m viá»‡c lÃ  báº¯t buá»™c")
    private LocalDate workDate;

    private String note;
}


