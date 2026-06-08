package com.example.hr.controllers;

import com.example.hr.service.HrAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-log")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HrAuditLogController {

    private final HrAuditLogService hrAuditLogService;

    @GetMapping
    public String listAuditLogs(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "50") int size,
                                @RequestParam(required = false) String q,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size);
        var logs = hrAuditLogService.findLogs(q, pageable);

        model.addAttribute("page", logs);
        model.addAttribute("q", q);
        return "admin/audit-log-list";
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportAuditLogs(@RequestParam(required = false) String q,
                                                  @RequestParam(defaultValue = "5000") int limit) {
        String csv = hrAuditLogService.exportCsv(q, limit);
        String filename = q == null || q.isBlank() ? "hr-audit-log.csv" : "hr-audit-log-filtered.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body('\uFEFF' + csv);
    }
}


