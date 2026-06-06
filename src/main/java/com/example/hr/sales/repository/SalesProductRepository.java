package com.example.hr.sales.repository;

import com.example.hr.models.User;
import com.example.hr.sales.entity.SalesProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesProductRepository extends JpaRepository<SalesProduct, Integer> {
    List<SalesProduct> findByActiveTrueOrderByCreatedAtDesc();

    List<SalesProduct> findByActiveTrueAndApprovalStatusOrderByCreatedAtDesc(String approvalStatus);

    List<SalesProduct> findBySellerOrderByCreatedAtDesc(User seller);
}
