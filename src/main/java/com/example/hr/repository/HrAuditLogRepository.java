package com.example.hr.repository;

import com.example.hr.models.HrAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HrAuditLogRepository extends JpaRepository<HrAuditLog, Long> {

    @Query("""
            SELECT h FROM HrAuditLog h WHERE
            LOWER(h.action) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(h.entityType) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(h.actorUsername) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(h.detail, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(COALESCE(h.entityId, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<HrAuditLog> search(@Param("q") String q, Pageable pageable);
}
