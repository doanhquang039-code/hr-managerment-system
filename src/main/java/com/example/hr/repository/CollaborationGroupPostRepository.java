package com.example.hr.repository;

import com.example.hr.models.CollaborationGroup;
import com.example.hr.models.CollaborationGroupPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationGroupPostRepository extends JpaRepository<CollaborationGroupPost, Integer> {

    @Query("""
            SELECT p FROM CollaborationGroupPost p
            WHERE p.group = :group
            ORDER BY p.pinned DESC, p.createdAt DESC
            """)
    List<CollaborationGroupPost> findFeedByGroup(CollaborationGroup group);
}


