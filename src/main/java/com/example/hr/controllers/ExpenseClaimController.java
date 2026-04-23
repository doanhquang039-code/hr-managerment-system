package com.example.hr.controllers;

import com.example.hr.enums.ExpenseStatus;
import com.example.hr.models.ExpenseClaim;
import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.CloudinaryService;
import com.example.hr.service.ExpenseClaimService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class ExpenseClaimController {

    private final ExpenseClaimService expenseClaimService;
    private final UserRepository userRepository;
    private final AuthUserHelper authUserHelper;
    private final CloudinaryService cloudinaryService;

    public ExpenseClaimController(ExpenseClaimService expenseClaimService,
                                  UserRepository userRepository,
                                  AuthUserHelper authUserHelper,
                                  CloudinaryService cloudinaryService) {
        this.expenseClaimService = expenseClaimService;
        this.userRepository = userRepository;
        this.authUserHelper = authUserHelper;
        this.cloudinaryService = cloudinaryService;
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/expenses")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String adminList(@RequestParam(required = false) String status, Model model) {
        List<ExpenseClaim> claims;
        if (status != null && !status.isBlank()) {
            claims = expenseClaimService.findByStatus(ExpenseStatus.valueOf(status));
        } else {
            claims = expenseClaimService.findAll();
        }

        model.addAttribute("claims", claims);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("countPending", expenseClaimService.countByStatus(ExpenseStatus.PENDING));
        model.addAttribute("countApproved", expenseClaimService.countByStatus(ExpenseStatus.APPROVED));
        model.addAttribute("countPaid", expenseClaimService.countByStatus(ExpenseStatus.PAID));
        model.addAttribute("countRejected", expenseClaimService.countByStatus(ExpenseStatus.REJECTED));
        model.addAttribute("totalPending", expenseClaimService.getTotalPendingAmount());
        return "admin/expense-list";
    }

    @GetMapping("/admin/expenses/approve/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String approve(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        User approver = authUserHelper.getCurrentUser(auth);
        expenseClaimService.approve(id, approver);
        ra.addFlashAttribute("success", "Đã duyệt yêu cầu chi phí!");
        return "redirect:/admin/expenses";
    }

    @PostMapping("/admin/expenses/reject/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String reject(@PathVariable Integer id,
                         @RequestParam String reason,
                         Authentication auth,
                         RedirectAttributes ra) {
        User approver = authUserHelper.getCurrentUser(auth);
        expenseClaimService.reject(id, approver, reason);
        ra.addFlashAttribute("success", "Đã từ chối yêu cầu chi phí!");
        return "redirect:/admin/expenses";
    }

    @GetMapping("/admin/expenses/paid/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String markPaid(@PathVariable Integer id, RedirectAttributes ra) {
        expenseClaimService.markPaid(id);
        ra.addFlashAttribute("success", "Đã đánh dấu đã thanh toán!");
        return "redirect:/admin/expenses";
    }

    @GetMapping("/admin/expenses/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        expenseClaimService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa yêu cầu chi phí!");
        return "redirect:/admin/expenses";
    }

    // ==================== USER ====================

    @GetMapping("/user1/expenses")
    @PreAuthorize("isAuthenticated()")
    public String userExpenses(Authentication auth, Model model) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        List<ExpenseClaim> myClaims = expenseClaimService.findByUser(currentUser.getId());
        model.addAttribute("myClaims", myClaims);
        model.addAttribute("newClaim", new ExpenseClaim());
        model.addAttribute("currentUser", currentUser);
        return "user1/expenses";
    }

    @PostMapping("/user1/expenses/submit")
    @PreAuthorize("isAuthenticated()")
    public String submitClaim(@ModelAttribute ExpenseClaim claim,
                              @RequestParam(name = "receiptFile", required = false) MultipartFile receiptFile,
                              Authentication auth,
                              RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        if (receiptFile != null && !receiptFile.isEmpty()) {
            String contentType = receiptFile.getContentType() != null ? receiptFile.getContentType() : "";
            boolean supportedType = contentType.startsWith("image/")
                    || "application/pdf".equalsIgnoreCase(contentType);
            if (!supportedType) {
                ra.addFlashAttribute("success", "Chỉ được tải lên ảnh hoặc file PDF cho chứng từ.");
                return "redirect:/user1/expenses";
            }
            try {
                Object secureUrl = cloudinaryService.uploadReceipt(receiptFile, "hrms/expense-receipts")
                        .get("secure_url");
                if (secureUrl != null) {
                    claim.setReceiptUrl(secureUrl.toString());
                }
            } catch (IOException e) {
                ra.addFlashAttribute("success", "Tải lên chứng từ thất bại: " + e.getMessage());
                return "redirect:/user1/expenses";
            }
        }

        claim.setUser(currentUser);
        expenseClaimService.save(claim);
        ra.addFlashAttribute("success", "Đã gửi yêu cầu hoàn tiền thành công!");
        return "redirect:/user1/expenses";
    }

    @GetMapping("/user1/expenses/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String userDelete(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        User currentUser = authUserHelper.getCurrentUser(auth);
        expenseClaimService.findById(id).ifPresent(c -> {
            if (c.getUser().getId().equals(currentUser.getId())
                    && c.getStatus() == ExpenseStatus.PENDING) {
                expenseClaimService.delete(id);
            }
        });
        ra.addFlashAttribute("success", "Đã hủy yêu cầu chi phí!");
        return "redirect:/user1/expenses";
    }
}
