package com.example.hr.controllers;

import com.example.hr.models.ExpenseClaim;
import com.example.hr.repository.ExpenseClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseClaimRepository expenseClaimRepository;

    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public String listExpenses(Model model) {
        List<ExpenseClaim> expenses = expenseClaimRepository.findAll();
        model.addAttribute("expenses", expenses);
        model.addAttribute("pageTitle", "Quáº£n lÃ½ Chi phÃ­");
        return "admin/expenses";
    }
}


