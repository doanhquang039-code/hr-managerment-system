package com.example.hr.dto;

import com.example.hr.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeSkillDTO {

    @NotNull(message = "userId là bắt buộc")
    private Integer userId;

    @NotBlank(message = "Tên kỹ năng là bắt buộc")
    private String skillName;

    private String skillCategory = "TECHNICAL"; // TECHNICAL, SOFT, LANGUAGE, MANAGEMENT

    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    private BigDecimal yearsExp = BigDecimal.ZERO;

    private Boolean isCertified = false;

    private String certificateName;

    private String certificateUrl;

    private LocalDate issuedDate;

    private LocalDate expiryDate;

    private String notes;
}
