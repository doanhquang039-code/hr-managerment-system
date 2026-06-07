package com.example.hr.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseClaimDTO {

    @NotNull(message = "userId lÃ  báº¯t buá»™c")
    private Integer userId;

    @NotBlank(message = "TiÃªu Ä‘á» lÃ  báº¯t buá»™c")
    private String claimTitle;

    private String category = "OTHER"; // TRAVEL, MEAL, EQUIPMENT, TRAINING, OTHER

    @NotNull(message = "Sá»‘ tiá»n lÃ  báº¯t buá»™c")
    @DecimalMin(value = "0.01", message = "Sá»‘ tiá»n pháº£i > 0")
    private BigDecimal amount;

    private String currency = "VND";

    @NotNull(message = "NgÃ y chi phÃ­ lÃ  báº¯t buá»™c")
    private LocalDate expenseDate;

    private String description;

    private String receiptUrl;

    private String projectCode;
}


