package com.example.hr.repository;

import com.example.hr.models.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, Integer> {

    Optional<GroupRole> findByName(String name);

    List<GroupRole> findAllByOrderBySortOrderAscNameAsc();

    boolean existsByName(String name);

    /** Count how many users are currently assigned this role */
    @Query("SELECT COUNT(u) FROM User u WHERE u.groupRole.id = :roleId")
    long countUsersByRoleId(Integer roleId);
}
