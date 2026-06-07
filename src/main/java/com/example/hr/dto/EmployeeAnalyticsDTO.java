package com.example.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO phÃ¢n tÃ­ch nhÃ¢n viÃªn nÃ¢ng cao.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAnalyticsDTO {

    private long totalHeadcount;
    private double avgTenureMonths;
    private double turnoverRatePercent;
    private long newHiresThisYear;
    private long terminationsThisYear;

    // PhÃ¢n bá»‘ theo phÃ²ng ban
    private Map<String, Long> headcountByDepartment;

    // PhÃ¢n bá»‘ theo vá»‹ trÃ­
    private Map<String, Long> headcountByPosition;

    // PhÃ¢n bá»‘ theo tráº¡ng thÃ¡i
    private Map<String, Long> headcountByStatus;

    // Xu hÆ°á»›ng headcount theo thÃ¡ng (12 thÃ¡ng)
    private List<MonthlyHeadcount> headcountTrending;

    // Chi phÃ­ nhÃ¢n sá»± trung bÃ¬nh
    private BigDecimal avgSalary;
    private BigDecimal totalSalaryCost;

    // Training metrics
    private double trainingCompletionRate;
    private long totalTrainingHours;

    // Warning metrics
    private long totalActiveWarnings;
    private double warningRatePercent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyHeadcount {
        private int year;
        private int month;
        private long headcount;
        private long newHires;
        private long terminations;
    }
}


