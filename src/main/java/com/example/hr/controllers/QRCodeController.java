package com.example.hr.controllers;

import com.example.hr.enums.QRCodeType;
import com.example.hr.models.QRCode;
import com.example.hr.models.QRCodeScan;
import com.example.hr.models.User;
import com.example.hr.service.QRCodeService;
import com.example.hr.user.service.UserService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final UserService userService;

    // ==================== ADMIN PAGES ====================

    @GetMapping("/admin/qrcode/list")
    public String listQRCodes(Model model) {
        List<QRCode> qrCodes = qrCodeService.getAllQRCodes();
        QRCodeService.QRCodeStats stats = qrCodeService.getStatistics();
        
        model.addAttribute("qrCodes", qrCodes);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "QR Code Management");
        
        return "admin/qrcode-list";
    }

    @GetMapping("/admin/qrcode/create")
    public String showCreateForm(Model model) {
        model.addAttribute("qrCode", new QRCode());
        model.addAttribute("types", QRCodeType.values());
        model.addAttribute("pageTitle", "Create QR Code");
        
        return "admin/qrcode-form";
    }

    @PostMapping("/admin/qrcode/create")
    public String createQRCode(@RequestParam QRCodeType type,
                               @RequestParam String name,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) String expiresAt,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(auth.getName());
        
        LocalDateTime expiry = null;
        if (expiresAt != null && !expiresAt.isEmpty()) {
            expiry = LocalDateTime.parse(expiresAt);
        }
        
        QRCode qrCode = qrCodeService.generateQRCode(type, name, location, description, user.getId(), expiry);
        
        redirectAttributes.addFlashAttribute("success", "QR Code created successfully!");
        return "redirect:/admin/qrcode/view/" + qrCode.getId();
    }

    @GetMapping("/admin/qrcode/view/{id}")
    public String viewQRCode(@PathVariable Integer id, Model model) {
        try {
            QRCode qrCode = qrCodeService.getQRCodeById(id);
            String qrCodeImage = qrCodeService.generateQRCodeImage(qrCode.getCode(), 300, 300);
            List<QRCodeScan> scans = qrCodeService.getQRCodeScans(id);
            
            model.addAttribute("qrCode", qrCode);
            model.addAttribute("qrCodeImage", qrCodeImage);
            model.addAttribute("scans", scans);
            model.addAttribute("pageTitle", "QR Code Details");
            
            return "admin/qrcode-view";
        } catch (WriterException | IOException e) {
            model.addAttribute("error", "Failed to generate QR Code image");
            return "redirect:/admin/qrcode/list";
        }
    }

    @GetMapping("/admin/qrcode/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        QRCode qrCode = qrCodeService.getQRCodeById(id);
        
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("types", QRCodeType.values());
        model.addAttribute("pageTitle", "Edit QR Code");
        
        return "admin/qrcode-form";
    }

    @PostMapping("/admin/qrcode/edit/{id}")
    public String updateQRCode(@PathVariable Integer id,
                               @RequestParam String name,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String description,
                               @RequestParam Boolean isActive,
                               RedirectAttributes redirectAttributes) {
        qrCodeService.updateQRCode(id, name, location, description, isActive);
        
        redirectAttributes.addFlashAttribute("success", "QR Code updated successfully!");
        return "redirect:/admin/qrcode/view/" + id;
    }

    @PostMapping("/admin/qrcode/delete/{id}")
    public String deleteQRCode(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        qrCodeService.deleteQRCode(id);
        
        redirectAttributes.addFlashAttribute("success", "QR Code deleted successfully!");
        return "redirect:/admin/qrcode/list";
    }

    // ==================== USER PAGES ====================

    @GetMapping("/qrcode/scan")
    public String showScanPage(Model model) {
        model.addAttribute("pageTitle", "Scan QR Code");
        return "user1/qrcode-scan";
    }

    @PostMapping("/qrcode/scan")
    @ResponseBody
    public ScanResponse processScan(@RequestParam String code,
                                   @RequestParam(required = false) String scanType,
                                   Authentication auth,
                                   HttpServletRequest request) {
        try {
            User user = userService.getUserByUsername(auth.getName());
            String ipAddress = request.getRemoteAddr();
            String deviceInfo = request.getHeader("User-Agent");
            
            if (scanType == null) scanType = "SCAN";
            
            QRCodeScan scan = qrCodeService.scanQRCode(code, user, scanType, ipAddress, deviceInfo);
            
            if (scan.getIsSuccessful()) {
                return new ScanResponse(true, "Scan successful!", scan.getQrCode().getName());
            } else {
                return new ScanResponse(false, scan.getErrorMessage(), null);
            }
        } catch (Exception e) {
            return new ScanResponse(false, "Invalid QR Code", null);
        }
    }

    @GetMapping("/qrcode/my-scans")
    public String showMyScans(Authentication auth, Model model) {
        User user = userService.getUserByUsername(auth.getName());
        List<QRCodeScan> scans = qrCodeService.getUserScans(user);
        List<QRCodeScan> todayScans = qrCodeService.getTodayScans(user);
        
        model.addAttribute("scans", scans);
        model.addAttribute("todayScans", todayScans);
        model.addAttribute("pageTitle", "My Scans");
        
        return "user1/my-scans";
    }

    // Response DTO
    record ScanResponse(boolean success, String message, String qrCodeName) {}
}


