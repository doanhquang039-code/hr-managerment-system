package com.example.hr.service;

import com.example.hr.dto.MonthlyReportDTO;
import com.example.hr.dto.PayrollSummaryDTO;
import com.example.hr.models.*;
import com.example.hr.repository.*;
import com.example.hr.util.ExcelExportUtil;
import com.example.hr.util.PayrollCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tổng hợp báo cáo HR.
 * Bao gồm: Monthly report, Payroll report, Headcount report, Training report, Asset report.
 */
@Service
public class ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationService.class);

    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final EmployeeWarningRepository warningRepository;
    private final EmployeeBenefitRepository benefitRepository;
    private final CompanyAssetRepository assetRepository;
    private final EmployeeDocumentRepository documentRepository;

    public ReportGenerationService(
            UserRepository userRepository,
            PayrollRepository payrollRepository,
            LeaveRequestRepository leaveRequestRepository,
            OvertimeRequestRepository overtimeRequestRepository,
            TrainingProgramRepository trainingProgramRepository,
            TrainingEnrollmentRepository trainingEnrollmentRepository,
            EmployeeWarningRepository warningRepository,
            EmployeeBenefitRepository benefitRepository,
            CompanyAssetRepository assetRepository,
            EmployeeDocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.trainingProgramRepository = trainingProgramRepository;
        this.trainingEnrollmentRepository = trainingEnrollmentRepository;
        this.warningRepository = warningRepository;
        this.benefitRepository = benefitRepository;
        this.assetRepository = assetRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Tạo Monthly HR Report.
     */
    public MonthlyReportDTO generateMonthlyReport(int year, int month) {
        log.info("Generating monthly report for {}/{}", month, year);
        MonthlyReportDTO report = new MonthlyReportDTO();
        report.setMonth(month);
        report.setYear(year);
        report.setGeneratedAt(LocalDateTime.now());

        // Employee stats
        long totalEmployees = userRepository.count();
        report.setTotalEmployees(totalEmployees);

        // Leave stats
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        long leaveRequests = leaveRequestRepository.findAll().stream()
                .filter(l -> !l.getStartDate().isAfter(endDate) && !l.getEndDate().isBefore(startDate))
                .count();
        report.setTotalLeaveRequests(leaveRequests);

        // OT stats
        long otRequests = overtimeRequestRepository.findAll().stream()
                .filter(ot -> ot.getOvertimeDate() != null)
                .filter(ot -> !ot.getOvertimeDate().isBefore(startDate) && !ot.getOvertimeDate().isAfter(endDate))
                .count();
        report.setTotalOvertimeRequests(otRequests);

        // Training stats
        long activeTrainings = trainingProgramRepository.findAll().stream()
                .filter(tp -> tp.getStatus() == com.example.hr.enums.TrainingStatus.IN_PROGRESS)
                .count();
        report.setActiveTrainingPrograms(activeTrainings);

        // Warning stats
        long activeWarnings = warningRepository.findAll().stream()
                .filter(w -> !Boolean.TRUE.equals(w.getIsAcknowledged()))
                .count();
        report.setActiveWarnings(activeWarnings);

        // Payroll total
        BigDecimal totalPayroll = payrollRepository.findAll().stream()
                .filter(p -> Objects.equals(p.getMonth(), month) && Objects.equals(p.getYear(), year))
                .map(Payroll::getNetSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalPayrollAmount(totalPayroll);

        log.info("Monthly report generated: employees={}, leaves={}, OT={}", totalEmployees, leaveRequests, otRequests);
        return report;
    }

    /**
     * Xuất Excel báo cáo nhân sự tổng hợp.
     */
    public byte[] exportEmployeeReport() throws IOException {
        log.info("Exporting employee report to Excel");

        List<String> headers = List.of(
                "Mã NV", "Họ tên", "Email", "Phòng ban",
                "Chức vụ", "Ngày vào làm", "Trạng thái", "Vai trò"
        );

        List<User> employees = userRepository.findAll();
        List<List<Object>> data = employees.stream()
                .map(emp -> {
                    List<Object> row = new ArrayList<>();
                    row.add(emp.getId());
                    row.add(emp.getFullName() != null ? emp.getFullName() : emp.getUsername());
                    row.add(emp.getEmail());
                    row.add(emp.getDepartment() != null ? emp.getDepartment().getDepartmentName() : "N/A");
                    row.add(emp.getPosition() != null ? emp.getPosition().getPositionName() : "N/A");
                    row.add(emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : "N/A");
                    row.add(emp.getStatus() != null ? emp.getStatus().name() : "ACTIVE");
                    row.add(emp.getRole() != null ? emp.getRole().name() : "N/A");
                    return row;
                })
                .collect(Collectors.toList());

        return ExcelExportUtil.exportToExcel("Danh sách nhân viên", headers, data);
    }

    /**
     * Xuất Excel báo cáo OT.
     */
    public byte[] exportOvertimeReport(int year, int month) throws IOException {
        log.info("Exporting overtime report for {}/{}", month, year);

        List<String> headers = List.of(
                "Mã NV", "Họ tên", "Ngày OT", "Số giờ",
                "Lý do", "Trạng thái", "Người duyệt", "Tiền OT"
        );

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<OvertimeRequest> requests = overtimeRequestRepository.findAll().stream()
                .filter(ot -> ot.getOvertimeDate() != null)
                .filter(ot -> !ot.getOvertimeDate().isBefore(start) && !ot.getOvertimeDate().isAfter(end))
                .toList();

        List<List<Object>> data = requests.stream()
                .map(ot -> {
                    List<Object> row = new ArrayList<>();
                    row.add(ot.getUser() != null ? ot.getUser().getId() : "N/A");
                    row.add(ot.getUser() != null ?
                            (ot.getUser().getFullName() != null ? ot.getUser().getFullName() : ot.getUser().getUsername())
                            : "N/A");
                    row.add(ot.getOvertimeDate());
                    row.add(ot.getTotalHours());
                    row.add(ot.getReason());
                    row.add(ot.getStatus().name());
                    row.add(ot.getApprovedBy() != null ? ot.getApprovedBy().getFullName() : "Chưa duyệt");
                    // OvertimeRequest hiện chưa lưu tiền OT; để 0 và có thể tính lại khi có hourlyRate.
                    row.add(BigDecimal.ZERO);
                    return row;
                })
                .collect(Collectors.toList());

        return ExcelExportUtil.exportToExcel("Báo cáo OT tháng " + month + "/" + year, headers, data);
    }

    /**
     * Xuất Excel danh sách tài sản.
     */
    public byte[] exportAssetReport() throws IOException {
        log.info("Exporting asset report to Excel");

        List<String> headers = List.of(
                "Mã TS", "Tên tài sản", "Loại", "Nguyên giá",
                "Giá trị hiện tại", "Ngày mua", "Trạng thái", "Bảo hành đến"
        );

        List<CompanyAsset> assets = assetRepository.findAll();
        List<List<Object>> data = assets.stream()
                .map(a -> {
                    List<Object> row = new ArrayList<>();
                    row.add(a.getAssetCode());
                    row.add(a.getAssetName());
                    row.add(a.getCategory());
                    row.add(a.getPurchasePrice());
                    row.add(a.getCurrentValue());
                    row.add(a.getPurchaseDate());
                    row.add(a.getStatus().name());
                    row.add(a.getWarrantyExpiry());
                    return row;
                })
                .collect(Collectors.toList());

        return ExcelExportUtil.exportToExcel("Danh sách tài sản", headers, data);
    }

    /**
     * Xuất Excel báo cáo training.
     */
    public byte[] exportTrainingReport() throws IOException {
        log.info("Exporting training report to Excel");

        List<String> headers = List.of(
                "Tên chương trình", "Loại", "Ngày bắt đầu", "Ngày kết thúc",
                "Số học viên", "Ngân sách", "Trạng thái", "Giảng viên"
        );

        List<TrainingProgram> programs = trainingProgramRepository.findAll();
        List<List<Object>> data = programs.stream()
                .map(tp -> {
                    List<Object> row = new ArrayList<>();
                    row.add(tp.getProgramName());
                    row.add(tp.getTrainingType());
                    row.add(tp.getStartDate());
                    row.add(tp.getEndDate());
                    row.add(tp.getEnrollments() != null ? tp.getEnrollments().size() : 0);
                    row.add(tp.getBudget());
                    row.add(tp.getStatus().name());
                    row.add(tp.getInstructor());
                    return row;
                })
                .collect(Collectors.toList());

        return ExcelExportUtil.exportToExcel("Báo cáo đào tạo", headers, data);
    }

    /**
     * Tổng hợp cost analysis theo phòng ban.
     */
    public Map<String, Map<String, BigDecimal>> generateDepartmentCostAnalysis(int year, int month) {
        Map<String, Map<String, BigDecimal>> analysis = new LinkedHashMap<>();

        // Get payroll by department
        List<Payroll> payrolls = payrollRepository.findAll().stream()
                .filter(p -> Objects.equals(p.getMonth(), month) && Objects.equals(p.getYear(), year))
                .toList();

        Map<String, List<Payroll>> byDept = payrolls.stream()
                .filter(p -> p.getUser() != null && p.getUser().getDepartment() != null)
                .collect(Collectors.groupingBy(p -> p.getUser().getDepartment().getDepartmentName()));

        for (Map.Entry<String, List<Payroll>> entry : byDept.entrySet()) {
            Map<String, BigDecimal> costs = new LinkedHashMap<>();
            BigDecimal totalSalary = entry.getValue().stream()
                    .map(Payroll::getNetSalary)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgSalary = entry.getValue().isEmpty() ? BigDecimal.ZERO
                    : totalSalary.divide(BigDecimal.valueOf(entry.getValue().size()), 0, RoundingMode.HALF_UP);

            costs.put("totalSalary", totalSalary);
            costs.put("avgSalary", avgSalary);
            costs.put("headcount", BigDecimal.valueOf(entry.getValue().size()));

            analysis.put(entry.getKey(), costs);
        }

        return analysis;
    }

    /**
     * Tạo yearly summary.
     */
    public Map<String, Object> generateYearlySummary(int year) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("year", year);

        // Monthly breakdowns
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", m);

            MonthlyReportDTO report = generateMonthlyReport(year, m);
            monthData.put("totalEmployees", report.getTotalEmployees());
            monthData.put("leaveRequests", report.getTotalLeaveRequests());
            monthData.put("overtimeRequests", report.getTotalOvertimeRequests());
            monthData.put("payrollAmount", report.getTotalPayrollAmount());

            monthlyData.add(monthData);
        }
        summary.put("monthlyBreakdown", monthlyData);

        // Training summary
        long totalPrograms = trainingProgramRepository.count();
        long completedPrograms = trainingProgramRepository.findAll().stream()
                .filter(tp -> tp.getStatus() == com.example.hr.enums.TrainingStatus.COMPLETED)
                .count();
        summary.put("totalTrainingPrograms", totalPrograms);
        summary.put("completedTrainingPrograms", completedPrograms);

        // Warning summary
        long totalWarnings = warningRepository.count();
        summary.put("totalWarningsIssued", totalWarnings);

        // Asset summary
        long totalAssets = assetRepository.count();
        BigDecimal totalAssetValue = assetRepository.findAll().stream()
                .map(CompanyAsset::getCurrentValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalAssets", totalAssets);
        summary.put("totalAssetValue", totalAssetValue);

        return summary;
    }
}
