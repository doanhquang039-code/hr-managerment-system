package com.example.hr.sales.repository;

import com.example.hr.sales.entity.SalesOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {
    @EntityGraph(attributePaths = {"customer", "createdBy", "items", "items.product", "items.product.seller"})
    List<SalesOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"customer", "createdBy", "items", "items.product", "items.product.seller"})
    List<SalesOrder> findByStatus(String status);

    @EntityGraph(attributePaths = {"customer", "createdBy", "items", "items.product", "items.product.seller"})
    List<SalesOrder> findByStatusOrderByCreatedAtDesc(String status);
}
