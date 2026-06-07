package com.example.hr.dto;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.KpiStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class KpiGoalDTO {

    @NotNull(message = "userId lÃ  báº¯t buá»™c")
    private Integer userId;

    private Integer departmentId;

    @NotBlank(message = "TiÃªu Ä‘á» KPI lÃ  báº¯t buá»™c")
    private String goalTitle;

    private String description;

    private String category = "INDIVIDUAL"; // INDIVIDUAL, TEAM, DEPARTMENT

    @NotNull(message = "GiÃ¡ trá»‹ má»¥c tiÃªu lÃ  báº¯t buá»™c")
    @DecimalMin(value = "0", message = "GiÃ¡ trá»‹ má»¥c tiÃªu pháº£i >= 0")
    private BigDecimal targetValue;

    private BigDecimal currentValue = BigDecimal.ZERO;

    private String unit;

    private BigDecimal weight = BigDecimal.ONE;

    @NotNull(message = "NgÃ y báº¯t Ä‘áº§u lÃ  báº¯t buá»™c")
    private LocalDate startDate;

    @NotNull(message = "NgÃ y káº¿t thÃºc lÃ  báº¯t buá»™c")
    private LocalDate endDate;

    private KpiStatus status = KpiStatus.ACTIVE;
}


