package com.example.hr.elasticsearch.repository;


import com.example.hr.department.entity.Department;
import com.example.hr.elasticsearch.document.EmployeeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Employee Search Repository
 * Repository cho Elasticsearch search
 */
@Repository
public interface EmployeeSearchRepository extends ElasticsearchRepository<EmployeeDocument, Integer> {

    /**
     * Search by full name
     */
    List<EmployeeDocument> findByFullNameContaining(String fullName);

    /**
     * Search by department
     */
    List<EmployeeDocument> findByDepartment(String department);

    /**
     * Search by position
     */
    List<EmployeeDocument> findByPosition(String position);

    /**
     * Search by skills
     */
    List<EmployeeDocument> findBySkillsContaining(String skill);

    /**
     * Search by status
     */
    List<EmployeeDocument> findByStatus(String status);
}
