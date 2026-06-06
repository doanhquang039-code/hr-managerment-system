package com.example.hr.payroll.service;

import com.example.hr.payroll.entity.Payroll;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayrollSecurityService {

    private final ObjectMapper objectMapper;

    public void attachEncryptedSalaryPayload(Payroll payroll) {
        if (payroll == null) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("payrollId", payroll.getId());
            payload.put("userId", payroll.getUser() != null ? payroll.getUser().getId() : null);
            payload.put("month", payroll.getMonth());
            payload.put("year", payroll.getYear());
            payload.put("baseSalary", nz(payroll.getBaseSalary()));
            payload.put("bonus", nz(payroll.getBonus()));
            payload.put("deductions", nz(payroll.getDeductions()));
            payload.put("netSalary", nz(netSalary(payroll)));
            payroll.setEncryptedSalaryPayload(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Could not create encrypted salary payload", e);
        }
    }

    private static BigDecimal netSalary(Payroll payroll) {
        BigDecimal base = nz(payroll.getBaseSalary());
        BigDecimal bonus = nz(payroll.getBonus());
        BigDecimal deductions = nz(payroll.getDeductions());
        return base.add(bonus).subtract(deductions);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
