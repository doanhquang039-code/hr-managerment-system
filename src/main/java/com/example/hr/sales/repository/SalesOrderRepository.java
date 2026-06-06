package com.example.hr.sales.repository;

import com.example.hr.sales.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {
    List<SalesOrder> findAllByOrderByCreatedAtDesc();
}
