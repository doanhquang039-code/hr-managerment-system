package com.example.hr.sales.repository;

import com.example.hr.sales.entity.SalesCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesCustomerRepository extends JpaRepository<SalesCustomer, Integer> {
    List<SalesCustomer> findByActiveTrueOrderByCreatedAtDesc();
}
