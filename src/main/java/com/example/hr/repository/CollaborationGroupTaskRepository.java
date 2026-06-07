package com.example.hr.repository;

import com.example.hr.models.CollaborationGroup;
import com.example.hr.models.CollaborationGroupTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationGroupTaskRepository extends JpaRepository<CollaborationGroupTask, Integer> {

    @Query("""
            SELECT t FROM CollaborationGroupTask t
            WHERE t.group = :group
            ORDER BY
                CASE t.status
                    WHEN 'IN_PROGRESS' THEN 0
                    WHEN 'TODO' THEN 1
                    ELSE 2
                END,
                t.dueDate ASC,
                t.createdAt DESC
            """)
    List<CollaborationGroupTask> findBoardByGroup(CollaborationGroup group);
}


