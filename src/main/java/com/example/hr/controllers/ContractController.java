package com.example.hr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hr.enums.UserStatus;
import com.example.hr.models.Contract;
import com.example.hr.repository.ContractRepository;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.HrAuditLogService;

import java.util.List;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping({ "/admin/contracts", "/manager/contracts" })
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ContractController {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HrAuditLogService hrAuditLogService;

   @GetMapping
public String list(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
    List<Contract> contracts = contractRepository.findAllWithUser(keyword);
    model.addAttribute("contracts", contracts);
    return "admin/contract-list";
}

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("contract", new Contract());
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        return "admin/contract-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("contract") Contract contract, Authentication auth) {
        boolean isNew = contract.getId() == null;
        contractRepository.save(contract);
        hrAuditLogService.log(auth, isNew ? "CONTRACT_CREATED" : "CONTRACT_UPDATED", "Contract",
                contract.getId() != null ? String.valueOf(contract.getId()) : null,
                "Loại: " + contract.getContractType()
                        + ", userId=" + (contract.getUser() != null ? contract.getUser().getId() : "?")
                        + ", hết hạn=" + contract.getExpiryDate(),
                null);
        return "redirect:/admin/contracts";
    }
    @GetMapping("/contracts")
public String listContracts(
        @RequestParam(required = false) String employeeName,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "startDate") String sortBy,
        Model model) {
    
    Sort sort = Sort.by(sortBy).descending();
    List<Contract> contracts = contractRepository.filterContracts(employeeName, type, null, sort);
    
    model.addAttribute("contracts", contracts);
    return "contract/list";
}
}
