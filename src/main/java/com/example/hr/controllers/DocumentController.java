package com.example.hr.controllers;

import com.example.hr.dto.EmployeeDocumentDTO;
import com.example.hr.enums.DocumentType;
import com.example.hr.models.EmployeeDocument;
import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.EmployeeDocumentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DocumentController {

    private final EmployeeDocumentService documentService;
    private final UserRepository userRepository;
    private final AuthUserHelper authUserHelper;

    public DocumentController(EmployeeDocumentService documentService,
                               UserRepository userRepository,
                               AuthUserHelper authUserHelper) {
        this.documentService = documentService;
        this.userRepository = userRepository;
        this.authUserHelper = authUserHelper;
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/documents")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminList(@RequestParam(required = false) Integer userId,
                             @RequestParam(required = false) String type,
                             Model model) {
        List<EmployeeDocument> docs;
        if (userId != null) {
            docs = documentService.getDocumentsByUser(userId);
        } else if (type != null && !type.isBlank()) {
            docs = documentService.getDocumentsByType(DocumentType.valueOf(type));
        } else {
            docs = documentService.getUnverifiedDocuments();
            model.addAttribute("showingUnverified", true);
        }

        model.addAttribute("documents", docs);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedType", type);
        model.addAttribute("expiringDocs", documentService.getExpiringSoonDocuments());
        model.addAttribute("expiredDocs", documentService.getExpiredDocuments());
        return "admin/document-list";
    }

    @GetMapping("/admin/documents/upload")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String uploadForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("docTypes", DocumentType.values());
        return "admin/document-upload";
    }

    @PostMapping("/admin/documents/upload")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String handleUpload(@RequestParam Integer userId,
                                @RequestParam String documentName,
                                @RequestParam String documentType,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String expiryDate,
                                @RequestParam("file") MultipartFile file,
                                RedirectAttributes ra) {
        try {
            EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
            dto.setUserId(userId);
            dto.setDocumentName(documentName);
            dto.setDocumentType(DocumentType.valueOf(documentType));
            dto.setDescription(description);
            if (expiryDate != null && !expiryDate.isBlank()) {
                dto.setExpiryDate(java.time.LocalDate.parse(expiryDate));
            }
            documentService.uploadDocument(dto, file);
            ra.addFlashAttribute("success", "Upload tài liệu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi upload: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @GetMapping("/admin/documents/verify/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String verify(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        User verifier = authUserHelper.getCurrentUser(auth);
        documentService.verifyDocument(id, verifier);
        ra.addFlashAttribute("success", "Đã xác minh tài liệu!");
        return "redirect:/admin/documents";
    }

    @GetMapping("/admin/documents/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        documentService.deleteDocument(id);
        ra.addFlashAttribute("success", "Đã xóa tài liệu!");
        return "redirect:/admin/documents";
    }

    // ==================== USER ====================

    @GetMapping("/user1/documents")
    @PreAuthorize("isAuthenticated()")
    public String userDocuments(Authentication auth, Model model) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        List<EmployeeDocument> myDocs = documentService.getDocumentsByUser(currentUser.getId()).stream()
                .sorted((a, b) -> {
                    if (a.getUploadedAt() == null && b.getUploadedAt() == null) return 0;
                    if (a.getUploadedAt() == null) return 1;
                    if (b.getUploadedAt() == null) return -1;
                    return b.getUploadedAt().compareTo(a.getUploadedAt());
                })
                .toList();
        model.addAttribute("myDocs", myDocs);
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("expiringCount", myDocs.stream().filter(d -> d.isExpiringSoon(30)).count());
        return "user1/documents";
    }

    @PostMapping("/user1/documents/upload")
    @PreAuthorize("isAuthenticated()")
    public String userUpload(@RequestParam String documentName,
                              @RequestParam String documentType,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String expiryDate,
                              @RequestParam("file") MultipartFile file,
                              Authentication auth,
                              RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        try {
            EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
            dto.setUserId(currentUser.getId());
            dto.setDocumentName(documentName);
            dto.setDocumentType(DocumentType.valueOf(documentType));
            dto.setDescription(description);
            if (expiryDate != null && !expiryDate.isBlank()) {
                dto.setExpiryDate(java.time.LocalDate.parse(expiryDate));
            }
            documentService.uploadDocument(dto, file);
            ra.addFlashAttribute("success", "Upload tài liệu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/user1/documents";
    }

    @GetMapping("/user1/documents/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String userDelete(@PathVariable Integer id,
                             Authentication auth,
                             RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        EmployeeDocument doc = documentService.getDocumentById(id);
        if (!doc.getUser().getId().equals(currentUser.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa tài liệu này.");
            return "redirect:/user1/documents";
        }
        if (Boolean.TRUE.equals(doc.getIsVerified())) {
            ra.addFlashAttribute("error", "Tài liệu đã được xác minh, không thể tự xóa.");
            return "redirect:/user1/documents";
        }

        documentService.deleteDocument(id);
        ra.addFlashAttribute("success", "Đã xóa tài liệu.");
        return "redirect:/user1/documents";
    }
}
