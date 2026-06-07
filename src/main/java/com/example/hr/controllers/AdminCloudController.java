package com.example.hr.controllers;


import com.example.hr.payroll.entity.Payroll;
import com.example.hr.service.AwsS3Service;
import com.example.hr.service.CloudStorageFacade;
import com.example.hr.service.GoogleDriveService;
import com.example.hr.service.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/cloud")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCloudController {

    @Autowired
    private CloudStorageFacade cloudStorageFacade;

    @Autowired(required = false)
    private AwsS3Service s3Service;

    @Autowired(required = false)
    private GoogleDriveService driveService;

    @Autowired
    private ReportGenerationService reportService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("cloudStatus", cloudStorageFacade.getHealthStatus());
        model.addAttribute("enabledServices", cloudStorageFacade.getEnabledServices());

        // S3 file listing
        if (s3Service != null) {
            try {
                model.addAttribute("s3Reports", s3Service.listFiles("reports/"));
                model.addAttribute("s3Payslips", s3Service.listFiles("payslips/"));
                model.addAttribute("s3AuditLogs", s3Service.listFiles("audit-logs/"));
                model.addAttribute("s3Online", s3Service.isHealthy());
            } catch (Exception e) {
                model.addAttribute("s3Online", false);
                model.addAttribute("s3Error", e.getMessage());
            }
        }

        // Drive status
        if (driveService != null) {
            model.addAttribute("driveOnline", driveService.isHealthy());
        }

        return "admin/cloud-dashboard";
    }

    /** Manual backup report lÃªn S3 */
    @PostMapping("/backup-report")
    public String backupReport(@RequestParam(defaultValue = "0") int month,
                                @RequestParam(defaultValue = "0") int year,
                                RedirectAttributes ra) {
        if (s3Service == null) {
            ra.addFlashAttribute("error", "AWS S3 chÆ°a Ä‘Æ°á»£c cáº¥u hÃ¬nh!");
            return "redirect:/admin/cloud";
        }
        try {
            int m = month > 0 ? month : LocalDate.now().getMonthValue();
            int y = year  > 0 ? year  : LocalDate.now().getYear();
            byte[] data = reportService.generateMonthlyReportBytes(m, y);
            String fileName = String.format("payroll-report-%d-%02d.csv", y, m);
            String url = s3Service.uploadReport(data, fileName, "text/csv");
            ra.addFlashAttribute("success", "ÄÃ£ backup bÃ¡o cÃ¡o lÃªn S3: " + fileName);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Backup tháº¥t báº¡i: " + e.getMessage());
        }
        return "redirect:/admin/cloud";
    }

    /** XÃ³a file trÃªn S3 */
    @PostMapping("/s3/delete")
    public String deleteS3File(@RequestParam String key, RedirectAttributes ra) {
        if (s3Service == null) {
            ra.addFlashAttribute("error", "S3 chÆ°a cáº¥u hÃ¬nh!");
            return "redirect:/admin/cloud";
        }
        try {
            s3Service.deleteFile(key);
            ra.addFlashAttribute("success", "ÄÃ£ xÃ³a file: " + key);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "XÃ³a tháº¥t báº¡i: " + e.getMessage());
        }
        return "redirect:/admin/cloud";
    }

    /** API: Cloud health status */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getStatus() {
        return cloudStorageFacade.getHealthStatus();
    }
}


