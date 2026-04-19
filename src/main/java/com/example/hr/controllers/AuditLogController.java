package com.example.hr.controllers;

import com.example.hr.models.HrAuditLog;
import com.example.hr.repository.HrAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-log")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    @Autowired
    private HrAuditLogRepository hrAuditLogRepository;

    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        int pageIndex = Math.max(0, page);
        var pageable = PageRequest.of(pageIndex, 30, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<HrAuditLog> result;
        if (q != null && !q.isBlank()) {
            result = hrAuditLogRepository.search(q.trim(), pageable);
        } else {
            result = hrAuditLogRepository.findAll(pageable);
        }

        model.addAttribute("page", result);
        model.addAttribute("q", q != null ? q : "");
        return "admin/audit-log-list";
    }
}
