package com.example.hr.controllers;

import com.example.hr.dto.EmployeeDocumentDTO;
import com.example.hr.models.EmployeeDocument;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
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
            docs = documentService.getDocumentsByType(type);
        } else {
            docs = documentService.getUnverifiedDocuments();
            model.addAttribute("showingUnverified", true);
        }

        model.addAttribute("documents", docs);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedType", type);
        return "admin/document-list";
    }

    @GetMapping("/admin/documents/upload")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String uploadForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/document-upload";
    }

    @PostMapping("/admin/documents/upload")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String handleUpload(@RequestParam Integer userId,
                                @RequestParam String fileName,
                                @RequestParam String documentType,
                                @RequestParam(required = false) String description,
                                @RequestParam("file") MultipartFile file,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            User uploadedBy = authUserHelper.getCurrentUser(auth);
            EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
            dto.setUserId(userId);
            dto.setFileName(fileName);
            dto.setDocumentType(documentType);
            dto.setDescription(description);
            documentService.uploadDocument(dto, file, uploadedBy);
            ra.addFlashAttribute("success", "Upload tÃ i liá»‡u thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lá»—i upload: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @GetMapping("/admin/documents/verify/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String verify(@PathVariable Long id, RedirectAttributes ra) {
        documentService.verifyDocument(id);
        ra.addFlashAttribute("success", "ÄÃ£ xÃ¡c minh tÃ i liá»‡u!");
        return "redirect:/admin/documents";
    }

    @GetMapping("/admin/documents/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        documentService.deleteDocument(id);
        ra.addFlashAttribute("success", "ÄÃ£ xÃ³a tÃ i liá»‡u!");
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
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
        model.addAttribute("myDocs", myDocs);
        model.addAttribute("currentUser", currentUser);
        return "user1/documents";
    }

    @PostMapping("/user1/documents/upload")
    @PreAuthorize("isAuthenticated()")
    public String userUpload(@RequestParam String fileName,
                              @RequestParam String documentType,
                              @RequestParam(required = false) String description,
                              @RequestParam("file") MultipartFile file,
                              Authentication auth,
                              RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        try {
            EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
            dto.setUserId(currentUser.getId());
            dto.setFileName(fileName);
            dto.setDocumentType(documentType);
            dto.setDescription(description);
            documentService.uploadDocument(dto, file, currentUser);
            ra.addFlashAttribute("success", "Upload tÃ i liá»‡u thÃ nh cÃ´ng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lá»—i: " + e.getMessage());
        }
        return "redirect:/user1/documents";
    }

    @GetMapping("/user1/documents/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String userDelete(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        EmployeeDocument doc = documentService.getDocumentById(id);
        if (!doc.getUser().getId().equals(currentUser.getId())) {
            ra.addFlashAttribute("error", "Báº¡n khÃ´ng cÃ³ quyá»n xÃ³a tÃ i liá»‡u nÃ y.");
            return "redirect:/user1/documents";
        }
        if (Boolean.TRUE.equals(doc.getIsVerified())) {
            ra.addFlashAttribute("error", "TÃ i liá»‡u Ä‘Ã£ Ä‘Æ°á»£c xÃ¡c minh, khÃ´ng thá»ƒ tá»± xÃ³a.");
            return "redirect:/user1/documents";
        }

        documentService.deleteDocument(id);
        ra.addFlashAttribute("success", "ÄÃ£ xÃ³a tÃ i liá»‡u.");
        return "redirect:/user1/documents";
    }
}


