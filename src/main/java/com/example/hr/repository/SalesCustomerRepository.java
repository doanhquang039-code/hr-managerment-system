package com.example.hr.repository;

import com.example.hr.models.SalesCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesCustomerRepository extends JpaRepository<SalesCustomer, Integer> {
    List<SalesCustomer> findByActiveTrueOrderByCreatedAtDesc();
}
