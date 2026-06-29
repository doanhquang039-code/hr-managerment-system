package com.example.hr.sales.repository;

import com.example.hr.models.User;
import com.example.hr.sales.entity.SalesProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesProductRepository extends JpaRepository<SalesProduct, Integer> {
    @EntityGraph(attributePaths = "seller")
    List<SalesProduct> findByActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "seller")
    List<SalesProduct> findByActiveTrueAndApprovalStatusOrderByCreatedAtDesc(String approvalStatus);

    @EntityGraph(attributePaths = "seller")
    List<SalesProduct> findBySellerOrderByCreatedAtDesc(User seller);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM SalesProduct p LEFT JOIN FETCH p.seller WHERE p.active = true AND p.approvalStatus = 'APPROVED' AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY p.createdAt DESC")
    List<SalesProduct> searchActiveProducts(@org.springframework.data.repository.query.Param("query") String query);
}
