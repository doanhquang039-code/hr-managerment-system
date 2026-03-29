package com.example.hr.repository;

import com.example.hr.models.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Integer> {

    // ✅ Thêm cái này — Controller đang gọi
    List<JobPosition> findByActiveTrue();

    // ✅ Đổi từ findByTitleContainingIgnoreCase → findByPositionNameContainingIgnoreCase
    List<JobPosition> findByPositionNameContainingIgnoreCase(String positionName);

    // ✅ Giữ lại
    List<JobPosition> findByActive(Boolean active);
}