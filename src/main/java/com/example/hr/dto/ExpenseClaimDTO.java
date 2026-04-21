package com.example.hr.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseClaimDTO {

    @NotNull(message = "userId là bắt buộc")
    private Integer userId;

    @NotBlank(message = "Tiêu đề là bắt buộc")
    private String claimTitle;

    private String category = "OTHER"; // TRAVEL, MEAL, EQUIPMENT, TRAINING, OTHER

    @NotNull(message = "Số tiền là bắt buộc")
    @DecimalMin(value = "0.01", message = "Số tiền phải > 0")
    private BigDecimal amount;

    private String currency = "VND";

    @NotNull(message = "Ngày chi phí là bắt buộc")
    private LocalDate expenseDate;

    private String description;

    private String receiptUrl;

    private String projectCode;
}
