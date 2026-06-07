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
 * Service quáº£n lÃ½ tÃ i liá»‡u nhÃ¢n viÃªn.
 * Há»— trá»£ upload, verify, expire tracking.
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
     * Láº¥y táº¥t cáº£ tÃ i liá»‡u cá»§a má»™t nhÃ¢n viÃªn.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getDocumentsByUser(Integer userId) {
        return documentRepository.findByUserId(userId);
    }

    /**
     * Láº¥y tÃ i liá»‡u theo ID.
     */
    @Transactional(readOnly = true)
    public EmployeeDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TÃ i liá»‡u khÃ´ng tá»“n táº¡i", id));
    }

    /**
     * Láº¥y tÃ i liá»‡u theo loáº¡i.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getDocumentsByType(String documentType) {
        return documentRepository.findByDocumentType(documentType);
    }

    /**
     * Upload tÃ i liá»‡u má»›i cho nhÃ¢n viÃªn.
     */
    public EmployeeDocument uploadDocument(EmployeeDocumentDTO dto, MultipartFile file, User uploadedBy) {
        // Validate user exists
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ¢n viÃªn", dto.getUserId()));

        // Validate file
        validateFile(file);

        // Upload to Cloudinary
        String fileUrl;
        try {
            Map<?, ?> uploadResult = cloudinaryService.upload(file);
            fileUrl = (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new FileUploadException("KhÃ´ng thá»ƒ upload file: " + e.getMessage(), e);
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
     * Upload tÃ i liá»‡u khÃ´ng cÃ³ file (chá»‰ metadata).
     */
    public EmployeeDocument createDocumentMetadata(EmployeeDocumentDTO dto, User uploadedBy) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ¢n viÃªn", dto.getUserId()));

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
     * Cáº­p nháº­t thÃ´ng tin tÃ i liá»‡u.
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
     * XÃ¡c minh tÃ i liá»‡u.
     */
    public EmployeeDocument verifyDocument(Long documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        document.setIsVerified(true);
        return documentRepository.save(document);
    }

    /**
     * Há»§y xÃ¡c minh tÃ i liá»‡u.
     */
    public EmployeeDocument unverifyDocument(Long documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        document.setIsVerified(false);
        return documentRepository.save(document);
    }

    /**
     * XÃ³a tÃ i liá»‡u.
     */
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("TÃ i liá»‡u khÃ´ng tá»“n táº¡i", id);
        }
        documentRepository.deleteById(id);
    }

    /**
     * Láº¥y tÃ i liá»‡u chÆ°a xÃ¡c minh.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDocument> getUnverifiedDocuments() {
        return documentRepository.findAll().stream()
                .filter(doc -> !doc.getIsVerified())
                .toList();
    }

    /**
     * Kiá»ƒm tra nhÃ¢n viÃªn Ä‘Ã£ cÃ³ loáº¡i tÃ i liá»‡u chÆ°a.
     */
    @Transactional(readOnly = true)
    public boolean hasDocument(Integer userId, String documentType) {
        return documentRepository.existsByUserIdAndDocumentType(userId, documentType);
    }

    // --- Private helpers ---

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File quÃ¡ lá»›n. Tá»‘i Ä‘a 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new FileUploadException("Loáº¡i file khÃ´ng Ä‘Æ°á»£c há»— trá»£. Chá»‰ cháº¥p nháº­n: PDF, JPEG, PNG, DOC, DOCX");
        }
    }
}


