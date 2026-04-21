package com.example.hr.repository;

import com.example.hr.enums.SkillLevel;
import com.example.hr.models.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Integer> {

    List<EmployeeSkill> findByUserId(Integer userId);

    List<EmployeeSkill> findByUserIdAndSkillCategory(Integer userId, String category);

    Optional<EmployeeSkill> findByUserIdAndSkillName(Integer userId, String skillName);

    List<EmployeeSkill> findBySkillNameContainingIgnoreCase(String keyword);

    List<EmployeeSkill> findBySkillLevel(SkillLevel level);

    @Query("SELECT DISTINCT es.skillName FROM EmployeeSkill es WHERE es.skillCategory = :category")
    List<String> findDistinctSkillNamesByCategory(@Param("category") String category);

    @Query("SELECT es FROM EmployeeSkill es WHERE es.isCertified = true AND es.user.id = :userId")
    List<EmployeeSkill> findCertifiedSkillsByUser(@Param("userId") Integer userId);

    boolean existsByUserIdAndSkillName(Integer userId, String skillName);
}
