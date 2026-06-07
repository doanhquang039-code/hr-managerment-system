package com.example.hr.repository;

import com.example.hr.models.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    
    List<Shift> findByIsActiveTrue();
    
    List<Shift> findByNameContainingIgnoreCase(String name);
}


