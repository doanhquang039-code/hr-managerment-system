package com.example.hr.repository;

import com.example.hr.models.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {
    List<SalesOrder> findAllByOrderByCreatedAtDesc();
}
