package com.example.hr.dto;

import com.example.hr.enums.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBenefitDTO {

    private Integer id;

    @NotNull(message = "User ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Integer userId;

    @NotNull(message = "Loáº¡i phÃºc lá»£i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private BenefitType benefitType;

    @NotBlank(message = "TÃªn phÃºc lá»£i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String benefitName;

    private String description;
    private BigDecimal monetaryValue;

    @NotNull(message = "NgÃ y báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate startDate;

    private LocalDate endDate;
    private String provider;
    private String policyNumber;
}


