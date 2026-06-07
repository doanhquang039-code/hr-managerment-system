package com.example.hr.recruitment.repository;

import com.example.hr.recruitment.entity.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Integer> {

    // âœ… ThÃªm cÃ¡i nÃ y â€” Controller Ä‘ang gá»i
    List<JobPosition> findByActiveTrue();

    // âœ… Äá»•i tá»« findByTitleContainingIgnoreCase â†’ findByPositionNameContainingIgnoreCase
    List<JobPosition> findByPositionNameContainingIgnoreCase(String positionName);

    // âœ… Giá»¯ láº¡i
    List<JobPosition> findByActive(Boolean active);

    @org.springframework.data.jpa.repository.Query("SELECT j FROM JobPosition j WHERE j.active = true " +
           "AND (:keyword IS NULL OR LOWER(j.positionName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:level IS NULL OR j.jobLevel = :level)")
    org.springframework.data.domain.Page<JobPosition> searchPositions(@org.springframework.data.repository.query.Param("keyword") String keyword, 
                                                                      @org.springframework.data.repository.query.Param("level") Integer level, 
                                                                      org.springframework.data.domain.Pageable pageable);
}

