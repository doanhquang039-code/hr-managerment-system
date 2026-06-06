package com.example.hr.service;

import com.example.hr.dto.EmployeeDocumentDTO;
import com.example.hr.exception.FileUploadException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.EmployeeDocument;
import com.example.hr.models.User;
import com.example.hr.repository.EmployeeDocumentRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public EmployeeDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại", id));
    }

    /**
     * Lấy tài liệu theo loại.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getDocumentsByType(String documentType) {
        return documentRepository.findByDocumentType(documentType);
    }

    /**
     * Upload tài liệu mới cho nhân viên.
     */
    public EmployeeDocument uploadDocument(EmployeeDocumentDTO dto, MultipartFile file, User uploadedBy) {
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
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(fileUrl);
        document.setFileSize(String.valueOf(file.getSize()));
        document.setMimeType(file.getContentType());
        document.setDescription(dto.getDescription());
        document.setUploadedBy(uploadedBy);
        document.setIsVerified(false);
        document.setIsConfidential(dto.getIsConfidential() != null ? dto.getIsConfidential() : false);

        return documentRepository.save(document);
    }

    /**
     * Upload tài liệu không có file (chỉ metadata).
     */
    public EmployeeDocument createDocumentMetadata(EmployeeDocumentDTO dto, User uploadedBy) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", dto.getUserId()));

        EmployeeDocument document = new EmployeeDocument();
        document.setUser(user);
        document.setDocumentType(dto.getDocumentType());
        document.setFileName(dto.getFileName());
        document.setFileUrl(dto.getFileUrl());
        document.setFileSize("0");
        document.setMimeType("application/octet-stream");
        document.setDescription(dto.getDescription());
        document.setUploadedBy(uploadedBy);
        document.setIsVerified(false);
        document.setIsConfidential(dto.getIsConfidential() != null ? dto.getIsConfidential() : false);

        return documentRepository.save(document);
    }

    /**
     * Cập nhật thông tin tài liệu.
     */
    public EmployeeDocument updateDocument(Long id, EmployeeDocumentDTO dto) {
        EmployeeDocument document = getDocumentById(id);

        if (dto.getFileName() != null) document.setFileName(dto.getFileName());
        if (dto.getDocumentType() != null) document.setDocumentType(dto.getDocumentType());
        if (dto.getDescription() != null) document.setDescription(dto.getDescription());
        if (dto.getFileUrl() != null) document.setFileUrl(dto.getFileUrl());
        if (dto.getIsConfidential() != null) document.setIsConfidential(dto.getIsConfidential());

        return documentRepository.save(document);
    }

    /**
     * Xác minh tài liệu.
     */
    public EmployeeDocument verifyDocument(Long documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        document.setIsVerified(true);
        return documentRepository.save(document);
    }

    /**
     * Hủy xác minh tài liệu.
     */
    public EmployeeDocument unverifyDocument(Long documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        document.setIsVerified(false);
        return documentRepository.save(document);
    }

    /**
     * Xóa tài liệu.
     */
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tài liệu không tồn tại", id);
        }
        documentRepository.deleteById(id);
    }

    /**
     * Lấy tài liệu chưa xác minh.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getUnverifiedDocuments() {
        return documentRepository.findAll().stream()
                .filter(doc -> !doc.getIsVerified())
                .toList();
    }

    /**
     * Kiểm tra nhân viên đã có loại tài liệu chưa.
     */
    @Transactional(readOnly = true)
    public boolean hasDocument(Integer userId, String documentType) {
        return documentRepository.existsByUserIdAndDocumentType(userId, documentType);
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
