package com.example.hr.repository;

import com.example.hr.models.SalesProduct;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesProductRepository extends JpaRepository<SalesProduct, Integer> {
    List<SalesProduct> findByActiveTrueOrderByCreatedAtDesc();

    List<SalesProduct> findByActiveTrueAndApprovalStatusOrderByCreatedAtDesc(String approvalStatus);

    List<SalesProduct> findBySellerOrderByCreatedAtDesc(User seller);
}
