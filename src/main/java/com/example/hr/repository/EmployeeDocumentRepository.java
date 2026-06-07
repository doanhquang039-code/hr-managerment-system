package com.example.hr.repository;

import com.example.hr.enums.DocumentType;
import com.example.hr.models.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long>,
        JpaSpecificationExecutor<EmployeeDocument> {

    List<EmployeeDocument> findByUserId(Integer userId);

    List<EmployeeDocument> findByUserIdAndDocumentType(Integer userId, String documentType);

    List<EmployeeDocument> findByDocumentType(String documentType);

    long countByUserId(Integer userId);

    boolean existsByUserIdAndDocumentType(Integer userId, String documentType);
}


