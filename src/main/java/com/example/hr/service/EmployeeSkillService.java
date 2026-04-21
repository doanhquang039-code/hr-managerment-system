package com.example.hr.service;

import com.example.hr.models.EmployeeSkill;
import com.example.hr.repository.EmployeeSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeSkillService {

    private final EmployeeSkillRepository employeeSkillRepository;

    public EmployeeSkillService(EmployeeSkillRepository employeeSkillRepository) {
        this.employeeSkillRepository = employeeSkillRepository;
    }

    public List<EmployeeSkill> findAll() {
        return employeeSkillRepository.findAll();
    }

    public List<EmployeeSkill> findByUser(Integer userId) {
        return employeeSkillRepository.findByUserId(userId);
    }

    public List<EmployeeSkill> findByUserAndCategory(Integer userId, String category) {
        return employeeSkillRepository.findByUserIdAndSkillCategory(userId, category);
    }

    public List<EmployeeSkill> findCertifiedByUser(Integer userId) {
        return employeeSkillRepository.findCertifiedSkillsByUser(userId);
    }

    public Optional<EmployeeSkill> findById(Integer id) {
        return employeeSkillRepository.findById(id);
    }

    public EmployeeSkill save(EmployeeSkill skill) {
        if (skill.getId() == null) {
            skill.setCreatedAt(LocalDateTime.now());
        }
        skill.setUpdatedAt(LocalDateTime.now());
        return employeeSkillRepository.save(skill);
    }

    public void delete(Integer id) {
        employeeSkillRepository.deleteById(id);
    }

    public boolean existsByUserAndSkillName(Integer userId, String skillName) {
        return employeeSkillRepository.existsByUserIdAndSkillName(userId, skillName);
    }

    public List<String> getDistinctSkillsByCategory(String category) {
        return employeeSkillRepository.findDistinctSkillNamesByCategory(category);
    }
}
