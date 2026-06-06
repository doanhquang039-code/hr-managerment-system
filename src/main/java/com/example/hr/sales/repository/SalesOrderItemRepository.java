package com.example.hr.sales.repository;

import com.example.hr.sales.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Integer> {
}
