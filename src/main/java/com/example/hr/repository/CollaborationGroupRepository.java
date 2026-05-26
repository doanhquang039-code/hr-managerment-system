package com.example.hr.repository;

import com.example.hr.models.CollaborationGroup;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaborationGroupRepository extends JpaRepository<CollaborationGroup, Integer> {
    Optional<CollaborationGroup> findByName(String name);
    boolean existsByActiveTrueAndMembersContaining(User user);
}
