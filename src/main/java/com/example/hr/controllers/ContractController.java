package com.example.hr.controllers;


import com.example.hr.department.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hr.enums.UserStatus;
import com.example.hr.models.Contract;
import com.example.hr.repository.ContractRepository;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.HrAuditLogService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({ "/admin/contracts", "/manager/contracts" })
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ContractController {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private HrAuditLogService hrAuditLogService;

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "contractType", required = false) String contractType,
                       @RequestParam(name = "deptId", required = false) Integer deptId,
                       @RequestParam(name = "status", required = false) String status,
                       Model model) {
        List<Contract> contracts = contractRepository.findAllWithUser(keyword);

        if (deptId != null) {
            contracts = contracts.stream()
                    .filter(c -> c.getUser() != null
                            && c.getUser().getDepartment() != null
                            && deptId.equals(c.getUser().getDepartment().getId()))
                    .collect(Collectors.toList());
        }

        // Filter by contract type
        if (contractType != null && !contractType.isBlank()) {
            contracts = contracts.stream()
                    .filter(c -> contractType.equals(c.getContractType()))
                    .collect(Collectors.toList());
        }

        // Filter by status (active/expired)
        if ("ACTIVE".equals(status)) {
            contracts = contracts.stream()
                    .filter(c -> c.getExpiryDate() == null || !c.getExpiryDate().isBefore(java.time.LocalDate.now()))
                    .collect(Collectors.toList());
        } else if ("EXPIRED".equals(status)) {
            contracts = contracts.stream()
                    .filter(c -> c.getExpiryDate() != null && c.getExpiryDate().isBefore(java.time.LocalDate.now()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("contracts", contracts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedContractType", contractType);
        model.addAttribute("selectedDeptId", deptId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/contract-list";
    }

    @GetMapping("/add")
    public String showAddForm(@RequestParam(name = "editId", required = false) Integer editId, Model model) {
        if (editId != null) {
            Contract contract = contractRepository.findById(editId).orElse(new Contract());
            model.addAttribute("contract", contract);
        } else {
            model.addAttribute("contract", new Contract());
        }
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        return "admin/contract-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Contract contract = contractRepository.findById(id).orElse(new Contract());
        model.addAttribute("contract", contract);
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        return "admin/contract-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("contract") Contract contract, Authentication auth) {
        boolean isNew = contract.getId() == null;
        contractRepository.save(contract);
        hrAuditLogService.log(auth, isNew ? "CONTRACT_CREATED" : "CONTRACT_UPDATED", "Contract",
                contract.getId() != null ? String.valueOf(contract.getId()) : null,
                "Loáº¡i: " + contract.getContractType()
                        + ", userId=" + (contract.getUser() != null ? contract.getUser().getId() : "?")
                        + ", háº¿t háº¡n=" + contract.getExpiryDate(),
                null);
        return "redirect:/admin/contracts";
    }
}


