package com.example.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hr.models.Department;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    // Tìm các phòng ban con của một phòng ban cha
    List<Department> findByParentDepartmentId(Integer parentId);
    List<Department> findByDepartmentNameContainingIgnoreCase(String name);
}