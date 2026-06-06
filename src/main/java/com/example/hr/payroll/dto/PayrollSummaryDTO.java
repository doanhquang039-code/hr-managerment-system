package com.example.hr.payroll.dto;


import com.example.hr.payroll.entity.Payroll;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO tổng hợp bảng lương nâng cao.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSummaryDTO {

    private Integer userId;
    private String employeeName;
    private String departmentName;
    private String positionName;

    private int month;
    private int year;

    // Lương cơ bản
    private BigDecimal baseSalary;

    // Phụ cấp
    private BigDecimal allowance;

    // Tiền OT
    private BigDecimal overtimePay;
    private BigDecimal overtimeHours;

    // Thưởng
    private BigDecimal bonus;

    // Khấu trừ
    private BigDecimal socialInsurance;  // BHXH 8%
    private BigDecimal healthInsurance;  // BHYT 1.5%
    private BigDecimal unemploymentInsurance; // BHTN 1%
    private BigDecimal personalIncomeTax; // Thuế TNCN

    private BigDecimal totalDeductions;

    // Tổng thu nhập & lương thực nhận
    private BigDecimal grossSalary;
    private BigDecimal netSalary;

    /**
     * Tính tổng khấu trừ.
     */
    public void calculateTotalDeductions() {
        this.totalDeductions = BigDecimal.ZERO;
        if (socialInsurance != null) totalDeductions = totalDeductions.add(socialInsurance);
        if (healthInsurance != null) totalDeductions = totalDeductions.add(healthInsurance);
        if (unemploymentInsurance != null) totalDeductions = totalDeductions.add(unemploymentInsurance);
        if (personalIncomeTax != null) totalDeductions = totalDeductions.add(personalIncomeTax);
    }

    /**
     * Tính lương thực nhận.
     */
    public void calculateNetSalary() {
        this.grossSalary = BigDecimal.ZERO;
        if (baseSalary != null) grossSalary = grossSalary.add(baseSalary);
        if (allowance != null) grossSalary = grossSalary.add(allowance);
        if (overtimePay != null) grossSalary = grossSalary.add(overtimePay);
        if (bonus != null) grossSalary = grossSalary.add(bonus);

        calculateTotalDeductions();
        this.netSalary = grossSalary.subtract(totalDeductions != null ? totalDeductions : BigDecimal.ZERO);
    }
}
