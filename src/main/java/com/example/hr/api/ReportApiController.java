package com.example.hr.api;

import com.example.hr.dto.MonthlyReportDTO;
import com.example.hr.service.ReportGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * REST API controller cho xuáº¥t bÃ¡o cÃ¡o.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    private final ReportGenerationService reportService;

    public ReportApiController(ReportGenerationService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportDTO> monthlyReport(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(reportService.generateMonthlyReport(year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<Map<String, Object>> yearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(reportService.generateYearlySummary(year));
    }

    @GetMapping("/cost-analysis/{year}/{month}")
    public ResponseEntity<Map<String, Map<String, BigDecimal>>> costAnalysis(
            @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(reportService.generateDepartmentCostAnalysis(year, month));
    }

    @GetMapping("/export/employees")
    public ResponseEntity<byte[]> exportEmployees() throws IOException {
        byte[] data = reportService.exportEmployeeReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export/overtime/{year}/{month}")
    public ResponseEntity<byte[]> exportOvertime(@PathVariable int year, @PathVariable int month) throws IOException {
        byte[] data = reportService.exportOvertimeReport(year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=overtime_" + month + "_" + year + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export/assets")
    public ResponseEntity<byte[]> exportAssets() throws IOException {
        byte[] data = reportService.exportAssetReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assets.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export/training")
    public ResponseEntity<byte[]> exportTraining() throws IOException {
        byte[] data = reportService.exportTrainingReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=training.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}


