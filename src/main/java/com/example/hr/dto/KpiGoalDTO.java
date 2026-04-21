package com.example.hr.dto;

import com.example.hr.enums.KpiStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class KpiGoalDTO {

    @NotNull(message = "userId là bắt buộc")
    private Integer userId;

    private Integer departmentId;

    @NotBlank(message = "Tiêu đề KPI là bắt buộc")
    private String goalTitle;

    private String description;

    private String category = "INDIVIDUAL"; // INDIVIDUAL, TEAM, DEPARTMENT

    @NotNull(message = "Giá trị mục tiêu là bắt buộc")
    @DecimalMin(value = "0", message = "Giá trị mục tiêu phải >= 0")
    private BigDecimal targetValue;

    private BigDecimal currentValue = BigDecimal.ZERO;

    private String unit;

    private BigDecimal weight = BigDecimal.ONE;

    @NotNull(message = "Ngày bắt đầu là bắt buộc")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc là bắt buộc")
    private LocalDate endDate;

    private KpiStatus status = KpiStatus.ACTIVE;
}
