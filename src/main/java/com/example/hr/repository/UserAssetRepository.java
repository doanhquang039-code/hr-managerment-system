package com.example.hr.repository;

import com.example.hr.models.AssetAssignment;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAssetRepository extends JpaRepository<AssetAssignment, Long> {
    
    List<AssetAssignment> findByUserOrderByAssignedDateDesc(User user);
}


