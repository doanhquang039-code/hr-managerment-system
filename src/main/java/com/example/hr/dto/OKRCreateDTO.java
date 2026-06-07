package com.example.hr.dto;


import com.example.hr.department.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OKRCreateDTO {
    
    private String title;
    private String description;
    private String level; // COMPANY, DEPARTMENT, INDIVIDUAL
    private Long ownerId;
    private Long departmentId;
    private Long parentObjectiveId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<KeyResultDTO> keyResults;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyResultDTO {
        private String title;
        private String description;
        private String measurementType; // PERCENTAGE, NUMBER, BOOLEAN
        private Double targetValue;
        private String unit;
        private Integer weight;
    }
}


