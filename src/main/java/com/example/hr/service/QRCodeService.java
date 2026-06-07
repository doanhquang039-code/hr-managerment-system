package com.example.hr.service;

import com.example.hr.enums.QRCodeType;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.QRCode;
import com.example.hr.models.QRCodeScan;
import com.example.hr.models.User;
import com.example.hr.repository.QRCodeRepository;
import com.example.hr.repository.QRCodeScanRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final QRCodeScanRepository scanRepository;

    /**
     * Generate a new QR Code
     */
    public QRCode generateQRCode(QRCodeType type, String name, String location, String description, Integer createdBy, LocalDateTime expiresAt) {
        String code = UUID.randomUUID().toString();
        
        QRCode qrCode = new QRCode();
        qrCode.setCode(code);
        qrCode.setType(type);
        qrCode.setName(name);
        qrCode.setLocation(location);
        qrCode.setDescription(description);
        qrCode.setCreatedBy(createdBy);
        qrCode.setExpiresAt(expiresAt);
        qrCode.setIsActive(true);
        qrCode.setScanCount(0);
        qrCode.setCreatedAt(LocalDateTime.now());
        
        return qrCodeRepository.save(qrCode);
    }

    /**
     * Generate QR Code image as Base64
     */
    public String generateQRCodeImage(String code, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Validate and scan QR Code
     */
    public QRCodeScan scanQRCode(String code, User user, String scanType, String ipAddress, String deviceInfo) {
        QRCode qrCode = qrCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found"));
        
        // Validate QR Code
        if (!qrCode.isValid()) {
            QRCodeScan failedScan = new QRCodeScan();
            failedScan.setQrCode(qrCode);
            failedScan.setUser(user);
            failedScan.setScanType(scanType);
            failedScan.setIpAddress(ipAddress);
            failedScan.setDeviceInfo(deviceInfo);
            failedScan.setIsSuccessful(false);
            failedScan.setErrorMessage(qrCode.isExpired() ? "QR Code expired" : "QR Code inactive");
            return scanRepository.save(failedScan);
        }
        
        // Create successful scan
        QRCodeScan scan = new QRCodeScan();
        scan.setQrCode(qrCode);
        scan.setUser(user);
        scan.setScanType(scanType);
        scan.setIpAddress(ipAddress);
        scan.setDeviceInfo(deviceInfo);
        scan.setIsSuccessful(true);
        scan.setScannedAt(LocalDateTime.now());
        
        // Update QR Code stats
        qrCode.incrementScanCount();
        qrCodeRepository.save(qrCode);
        
        return scanRepository.save(scan);
    }

    /**
     * Get all QR Codes
     */
    @Transactional(readOnly = true)
    public List<QRCode> getAllQRCodes() {
        return qrCodeRepository.findAll();
    }

    /**
     * Get active QR Codes
     */
    @Transactional(readOnly = true)
    public List<QRCode> getActiveQRCodes() {
        return qrCodeRepository.findByIsActiveTrue();
    }

    /**
     * Get QR Codes by type
     */
    @Transactional(readOnly = true)
    public List<QRCode> getQRCodesByType(QRCodeType type) {
        return qrCodeRepository.findByType(type);
    }

    /**
     * Get valid QR Codes
     */
    @Transactional(readOnly = true)
    public List<QRCode> getValidQRCodes() {
        return qrCodeRepository.findAllValid(LocalDateTime.now());
    }

    /**
     * Get QR Code by ID
     */
    @Transactional(readOnly = true)
    public QRCode getQRCodeById(Integer id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code", id));
    }

    /**
     * Get QR Code by code string
     */
    @Transactional(readOnly = true)
    public QRCode getQRCodeByCode(String code) {
        return qrCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found"));
    }

    /**
     * Update QR Code
     */
    public QRCode updateQRCode(Integer id, String name, String location, String description, Boolean isActive) {
        QRCode qrCode = getQRCodeById(id);
        
        if (name != null) qrCode.setName(name);
        if (location != null) qrCode.setLocation(location);
        if (description != null) qrCode.setDescription(description);
        if (isActive != null) qrCode.setIsActive(isActive);
        
        return qrCodeRepository.save(qrCode);
    }

    /**
     * Delete QR Code
     */
    public void deleteQRCode(Integer id) {
        QRCode qrCode = getQRCodeById(id);
        qrCodeRepository.delete(qrCode);
    }

    /**
     * Get scan history for QR Code
     */
    @Transactional(readOnly = true)
    public List<QRCodeScan> getQRCodeScans(Integer qrCodeId) {
        QRCode qrCode = getQRCodeById(qrCodeId);
        return scanRepository.findByQrCodeOrderByScannedAtDesc(qrCode);
    }

    /**
     * Get scan history for User
     */
    @Transactional(readOnly = true)
    public List<QRCodeScan> getUserScans(User user) {
        return scanRepository.findByUserOrderByScannedAtDesc(user);
    }

    /**
     * Get today's scans for user
     */
    @Transactional(readOnly = true)
    public List<QRCodeScan> getTodayScans(User user) {
        return scanRepository.findByUserAndDate(user, LocalDateTime.now());
    }

    /**
     * Get statistics
     */
    @Transactional(readOnly = true)
    public QRCodeStats getStatistics() {
        long totalQRCodes = qrCodeRepository.count();
        long activeQRCodes = qrCodeRepository.countByIsActiveTrue();
        long totalScans = scanRepository.count();
        
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        long todayScans = scanRepository.countByDateRange(startOfDay, endOfDay);
        
        return new QRCodeStats(totalQRCodes, activeQRCodes, totalScans, todayScans);
    }

    // Inner class for statistics
    public record QRCodeStats(long totalQRCodes, long activeQRCodes, long totalScans, long todayScans) {}
}


