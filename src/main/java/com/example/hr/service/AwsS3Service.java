package com.example.hr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AWS S3 Service — backup báo cáo, payslip PDF, audit logs.
 * Chỉ active khi aws.s3.enabled=true.
 */
@ConditionalOnBean(S3Client.class)
public class AwsS3Service {

    private static final Logger log = LoggerFactory.getLogger(AwsS3Service.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:hrms-reports-backup}")
    private String bucket;

    @Value("${aws.s3.region:ap-southeast-1}")
    private String region;

    public AwsS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // ==================== UPLOAD ====================

    /**
     * Upload báo cáo PDF/Excel lên S3.
     * Key format: reports/2026/04/payroll-2026-04.pdf
     */
    public String uploadReport(byte[] data, String fileName, String contentType) {
        String key = buildReportKey(fileName);
        return upload(data, key, contentType);
    }

    /**
     * Upload payslip PDF cho nhân viên.
     * Key format: payslips/userId/2026-04-payslip.pdf
     */
    public String uploadPayslip(byte[] data, Integer userId, int month, int year) {
        String key = String.format("payslips/%d/%d-%02d-payslip.pdf", userId, year, month);
        return upload(data, key, "application/pdf");
    }

    /**
     * Upload audit log backup.
     * Key format: audit-logs/2026/04/audit-2026-04-22.json
     */
    public String uploadAuditLog(byte[] data, String date) {
        String key = String.format("audit-logs/%s/audit-%s.json",
                date.substring(0, 7).replace("-", "/"), date);
        return upload(data, key, "application/json");
    }

    /**
     * Upload document nhân viên.
     */
    public String uploadDocument(InputStream inputStream, long size,
                                  String fileName, String contentType) {
        String key = "documents/" + LocalDate.now().getYear() + "/" + fileName;
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .serverSideEncryption(ServerSideEncryption.AES256) // Encrypt at rest
                    .build(),
                    RequestBody.fromInputStream(inputStream, size));
            String url = getPublicUrl(key);
            log.info("Uploaded document to S3: {}", key);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload document to S3: {}", e.getMessage());
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    // ==================== DOWNLOAD / URL ====================

    /**
     * Tạo pre-signed URL có thời hạn (mặc định 1 giờ).
     */
    public String generatePresignedUrl(String key, int expiryMinutes) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build()) {

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryMinutes))
                    .getObjectRequest(r -> r.bucket(bucket).key(key))
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}: {}", key, e.getMessage());
            return null;
        }
    }

    // ==================== LIST / DELETE ====================

    /**
     * Liệt kê files trong một prefix (folder).
     */
    public List<String> listFiles(String prefix) {
        try {
            ListObjectsV2Response response = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build());
            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to list S3 files: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Xóa file trên S3.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            log.info("Deleted S3 file: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete S3 file {}: {}", key, e.getMessage());
        }
    }

    /**
     * Kiểm tra bucket tồn tại và accessible.
     */
    public boolean isHealthy() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== PRIVATE ====================

    private String upload(byte[] data, String key, String contentType) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build(),
                    RequestBody.fromBytes(data));
            String url = getPublicUrl(key);
            log.info("Uploaded to S3: {} ({} bytes)", key, data.length);
            return url;
        } catch (Exception e) {
            log.error("S3 upload failed for {}: {}", key, e.getMessage());
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    private String buildReportKey(String fileName) {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return "reports/" + yearMonth + "/" + fileName;
    }

    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
