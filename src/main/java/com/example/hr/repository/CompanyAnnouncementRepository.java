package com.example.hr.repository;

import com.example.hr.models.CompanyAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompanyAnnouncementRepository extends JpaRepository<CompanyAnnouncement, Integer> {

    @Query("""
            SELECT a FROM CompanyAnnouncement a
            WHERE a.active = true
              AND a.publishedAt <= :now
              AND (a.targetDepartment IS NULL
                   OR (:deptId IS NOT NULL AND a.targetDepartment.id = :deptId))
            ORDER BY a.publishedAt DESC
            """)
    List<CompanyAnnouncement> findPublishedVisibleForUser(
            @Param("now") LocalDateTime now,
            @Param("deptId") Integer deptId);

    List<CompanyAnnouncement> findAllByOrderByPublishedAtDesc();
}
