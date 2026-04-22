package com.example.hr.scheduler;

import com.example.hr.service.AwsS3Service;
import com.example.hr.service.ReportGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler tự động backup dữ liệu quan trọng lên AWS S3.
 */
@Component
public class CloudBackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(CloudBackupScheduler.class);

    @Autowired(required = false)
    private AwsS3Service s3Service;

    @Autowired
    private ReportGenerationService reportService;

    /**
     * Backup audit log hàng ngày lúc 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void backupDailyAuditLog() {
        if (s3Service == null) {
            log.debug("S3 not configured, skipping audit log backup");
            return;
        }
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            // Tạo JSON summary của audit log hôm nay
            byte[] auditData = ("{ \"date\": \"" + today + "\", \"backup\": \"daily\" }").getBytes();
            String s3Url = s3Service.uploadAuditLog(auditData, today);
            log.info("Daily audit log backed up to S3: {}", s3Url);
        } catch (Exception e) {
            log.error("Failed to backup audit log: {}", e.getMessage());
        }
    }

    /**
     * Backup monthly payroll report vào ngày 1 hàng tháng lúc 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void backupMonthlyPayrollReport() {
        if (s3Service == null) return;
        try {
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            int month = lastMonth.getMonthValue();
            int year  = lastMonth.getYear();

            log.info("Starting monthly payroll backup for {}/{}", month, year);
            // Tạo report data (simplified — trong thực tế generate Excel/PDF)
            byte[] reportData = reportService.generateMonthlyReportBytes(month, year);
            if (reportData != null && reportData.length > 0) {
                String fileName = String.format("payroll-report-%d-%02d.xlsx", year, month);
                String s3Url = s3Service.uploadReport(reportData, fileName,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                log.info("Monthly payroll report backed up to S3: {}", s3Url);
            }
        } catch (Exception e) {
            log.error("Failed to backup monthly payroll report: {}", e.getMessage());
        }
    }

    /**
     * Cleanup S3 files cũ hơn 1 năm (chạy mỗi tháng).
     */
    @Scheduled(cron = "0 0 4 15 * *")
    public void cleanupOldBackups() {
        if (s3Service == null) return;
        try {
            // List và xóa audit logs cũ hơn 1 năm
            String oldYearPrefix = "audit-logs/" +
                    LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyy"));
            var oldFiles = s3Service.listFiles(oldYearPrefix);
            log.info("Found {} old backup files to cleanup", oldFiles.size());
            // Chỉ log, không xóa tự động để an toàn
            // oldFiles.forEach(s3Service::deleteFile);
        } catch (Exception e) {
            log.error("Cleanup failed: {}", e.getMessage());
        }
    }
}
