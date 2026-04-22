package com.example.hr.service;

import com.example.hr.dto.EmployeeDocumentDTO;
import com.example.hr.enums.DocumentType;
import com.example.hr.exception.DuplicateResourceException;
import com.example.hr.exception.FileUploadException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.EmployeeDocument;
import com.example.hr.models.User;
import com.example.hr.repository.EmployeeDocumentRepository;
import com.example.hr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service quản lý tài liệu nhân viên.
 * Hỗ trợ upload, verify, expire tracking.
 */
@Service
@Transactional
public class EmployeeDocumentService {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf", "image/jpeg", "image/png", "image/jpg",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final EmployeeDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired(required = false)
    private CloudStorageFacade cloudStorageFacade;

    public EmployeeDocumentService(EmployeeDocumentRepository documentRepository,
                                     UserRepository userRepository,
                                     CloudinaryService cloudinaryService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Lấy tất cả tài liệu của một nhân viên.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getDocumentsByUser(Integer userId) {
        return documentRepository.findByUserId(userId);
    }

    /**
     * Lấy tài liệu theo ID.
     */
    @Transactional(readOnly = true)
    public EmployeeDocument getDocumentById(Integer id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu", id));
    }

    /**
     * Lấy tài liệu theo loại.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getDocumentsByType(DocumentType type) {
        return documentRepository.findByDocumentType(type);
    }

    /**
     * Upload tài liệu mới cho nhân viên.
     */
    public EmployeeDocument uploadDocument(EmployeeDocumentDTO dto, MultipartFile file) {
        // Validate user exists
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", dto.getUserId()));

        // Validate file
        validateFile(file);

        // Upload to Cloudinary
        String fileUrl;
        try {
            Map<?, ?> uploadResult = cloudinaryService.upload(file);
            fileUrl = (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new FileUploadException("Không thể upload file: " + e.getMessage(), e);
        }

        // Create document entity
        EmployeeDocument document = new EmployeeDocument();
        document.setUser(user);
        document.setDocumentType(dto.getDocumentType());
        document.setDocumentName(dto.getDocumentName());
        document.setFileUrl(fileUrl);
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setDescription(dto.getDescription());
        document.setExpiryDate(dto.getExpiryDate());
        document.setUploadedAt(LocalDateTime.now());
        document.setIsVerified(false);

        // Sync lên Google Drive / S3 nếu có
        if (cloudStorageFacade != null) {
            try {
                java.util.Map<String, String> cloudUrls = cloudStorageFacade
                        .uploadEmployeeDocument(file.getBytes(), dto.getDocumentName(),
                                file.getContentType(), dto.getUserId());
                if (cloudUrls.containsKey("driveFileId")) {
                    document.setDriveFileId(cloudUrls.get("driveFileId"));
                }
            } catch (Exception e) {
                // Log nhưng không fail — Cloudinary đã upload thành công
                org.slf4j.LoggerFactory.getLogger(getClass())
                        .warn("Cloud sync failed for document: {}", e.getMessage());
            }
        }

        return documentRepository.save(document);
    }

    /**
     * Upload tài liệu không có file (chỉ metadata).
     */
    public EmployeeDocument createDocumentMetadata(EmployeeDocumentDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", dto.getUserId()));

        EmployeeDocument document = new EmployeeDocument();
        document.setUser(user);
        document.setDocumentType(dto.getDocumentType());
        document.setDocumentName(dto.getDocumentName());
        document.setFileUrl(dto.getFileUrl());
        document.setDescription(dto.getDescription());
        document.setExpiryDate(dto.getExpiryDate());
        document.setUploadedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    /**
     * Cập nhật thông tin tài liệu.
     */
    public EmployeeDocument updateDocument(Integer id, EmployeeDocumentDTO dto) {
        EmployeeDocument document = getDocumentById(id);

        if (dto.getDocumentName() != null) document.setDocumentName(dto.getDocumentName());
        if (dto.getDocumentType() != null) document.setDocumentType(dto.getDocumentType());
        if (dto.getDescription() != null) document.setDescription(dto.getDescription());
        if (dto.getExpiryDate() != null) document.setExpiryDate(dto.getExpiryDate());
        if (dto.getFileUrl() != null) document.setFileUrl(dto.getFileUrl());

        return documentRepository.save(document);
    }

    /**
     * Xác minh tài liệu.
     */
    public EmployeeDocument verifyDocument(Integer documentId, User verifier) {
        EmployeeDocument document = getDocumentById(documentId);
        document.setIsVerified(true);
        document.setVerifiedBy(verifier);
        document.setVerifiedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }

    /**
     * Hủy xác minh tài liệu.
     */
    public EmployeeDocument unverifyDocument(Integer documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        document.setIsVerified(false);
        document.setVerifiedBy(null);
        document.setVerifiedAt(null);
        return documentRepository.save(document);
    }

    /**
     * Xóa tài liệu.
     */
    public void deleteDocument(Integer id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tài liệu", id);
        }
        documentRepository.deleteById(id);
    }

    /**
     * Lấy tài liệu chưa xác minh.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getUnverifiedDocuments() {
        return documentRepository.findUnverifiedDocuments();
    }

    /**
     * Lấy tài liệu sắp hết hạn (trong 30 ngày).
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getExpiringSoonDocuments() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        return documentRepository.findExpiringSoon(start, end);
    }

    /**
     * Lấy tài liệu đã hết hạn.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getExpiredDocuments() {
        return documentRepository.findExpiredDocuments(LocalDate.now());
    }

    /**
     * Đếm tài liệu theo loại.
     */
    @Transactional(readOnly = true)
    public Map<DocumentType, Long> countDocumentsByType() {
        return documentRepository.countByDocumentType().stream()
                .collect(Collectors.toMap(
                        row -> (DocumentType) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Kiểm tra nhân viên đã có loại tài liệu chưa.
     */
    @Transactional(readOnly = true)
    public boolean hasDocument(Integer userId, DocumentType type) {
        return documentRepository.existsByUserIdAndDocumentType(userId, type);
    }

    // --- Private helpers ---

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File quá lớn. Tối đa 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new FileUploadException("Loại file không được hỗ trợ. Chỉ chấp nhận: PDF, JPEG, PNG, DOC, DOCX");
        }
    }
}
