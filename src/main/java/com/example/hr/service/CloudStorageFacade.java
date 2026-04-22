package com.example.hr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade thống nhất cloud storage:
 * - Cloudinary: media (ảnh, video)
 * - AWS S3: backup báo cáo, payslip, audit logs
 * - Google Drive: sync tài liệu nhân viên
 *
 * Tự động fallback nếu service không available.
 */
@Service
public class CloudStorageFacade {

    private static final Logger log = LoggerFactory.getLogger(CloudStorageFacade.class);

    @Autowired(required = false)
    private AwsS3Service s3Service;

    @Autowired(required = false)
    private GoogleDriveService driveService;

    @Autowired(required = false)
    private FirebaseNotificationService firebaseService;

    // ==================== REPORT BACKUP ====================

    /**
     * Backup báo cáo lên S3 (nếu có) + Google Drive (nếu có).
     * @return Map chứa URLs từ các services
     */
    public Map<String, String> backupReport(byte[] data, String fileName, String mimeType) {
        Map<String, String> urls = new java.util.HashMap<>();

        if (s3Service != null) {
            try {
                String s3Url = s3Service.uploadReport(data, fileName, mimeType);
                urls.put("s3", s3Url);
                log.info("Report backed up to S3: {}", fileName);
            } catch (Exception e) {
                log.warn("S3 backup failed for {}: {}", fileName, e.getMessage());
            }
        }

        if (driveService != null) {
            try {
                String fileId = driveService.uploadReport(data, fileName, mimeType);
                String link = driveService.getWebViewLink(fileId);
                urls.put("drive", link);
                log.info("Report synced to Drive: {}", fileName);
            } catch (Exception e) {
                log.warn("Drive sync failed for {}: {}", fileName, e.getMessage());
            }
        }

        return urls;
    }

    /**
     * Backup payslip PDF lên S3.
     */
    public String backupPayslip(byte[] pdfData, Integer userId, int month, int year) {
        if (s3Service == null) return null;
        try {
            return s3Service.uploadPayslip(pdfData, userId, month, year);
        } catch (Exception e) {
            log.warn("Payslip S3 backup failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Upload tài liệu nhân viên lên Drive + S3.
     */
    public Map<String, String> uploadEmployeeDocument(byte[] data, String fileName,
                                                        String mimeType, Integer userId) {
        Map<String, String> urls = new java.util.HashMap<>();

        if (driveService != null) {
            try {
                String fileId = driveService.uploadEmployeeDocument(data, fileName, mimeType, userId);
                String link = driveService.getWebViewLink(fileId);
                urls.put("drive", link);
                urls.put("driveFileId", fileId);
            } catch (Exception e) {
                log.warn("Drive document upload failed: {}", e.getMessage());
            }
        }

        return urls;
    }

    // ==================== PUSH NOTIFICATIONS ====================

    /**
     * Gửi push notification qua Firebase (nếu có).
     */
    public void pushNotification(String fcmToken, String title, String body,
                                  Map<String, String> data) {
        if (firebaseService == null || fcmToken == null || fcmToken.isBlank()) return;
        firebaseService.sendToDevice(fcmToken, title, body, data);
    }

    /**
     * Broadcast thông báo đến tất cả nhân viên.
     */
    public void broadcastAnnouncement(String title, String content) {
        if (firebaseService == null) return;
        firebaseService.broadcastAnnouncement(title, content);
    }

    // ==================== HEALTH CHECK ====================

    /**
     * Kiểm tra trạng thái tất cả cloud services.
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new java.util.LinkedHashMap<>();

        status.put("s3", s3Service != null ? (s3Service.isHealthy() ? "online" : "error") : "disabled");
        status.put("googleDrive", driveService != null ? (driveService.isHealthy() ? "online" : "error") : "disabled");
        status.put("firebase", firebaseService != null ? "online" : "disabled");

        return status;
    }

    public List<String> getEnabledServices() {
        List<String> services = new ArrayList<>();
        if (s3Service != null) services.add("AWS S3");
        if (driveService != null) services.add("Google Drive");
        if (firebaseService != null) services.add("Firebase FCM");
        return services;
    }
}
