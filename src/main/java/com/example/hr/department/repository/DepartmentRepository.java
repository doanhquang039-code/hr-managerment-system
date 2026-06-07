package com.example.hr.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hr.department.entity.Department;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    // TÃ¬m cÃ¡c phÃ²ng ban con cá»§a má»™t phÃ²ng ban cha
    List<Department> findByParentDepartmentId(Integer parentId);
    List<Department> findByDepartmentNameContainingIgnoreCase(String name);
}

