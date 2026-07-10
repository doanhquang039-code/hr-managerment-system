package com.example.hr.config;

import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.department.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class SidebarAdvice {

    private final DepartmentRepository departmentRepository;

    @ModelAttribute("sidebarDepartments")
    public List<Department> getSidebarDepartments() {
        return departmentRepository.findAll();
    }
}
