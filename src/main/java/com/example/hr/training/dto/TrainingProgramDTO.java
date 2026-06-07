package com.example.hr.training.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramDTO {

    private Integer id;

    @NotBlank(message = "TÃªn chÆ°Æ¡ng trÃ¬nh khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String programName;

    private String description;
    private String instructor;
    private Integer departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxCapacity;
    private String location;
    private String trainingType;
    private BigDecimal budget;

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}

