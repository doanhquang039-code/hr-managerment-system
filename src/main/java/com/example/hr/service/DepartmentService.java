package com.example.hr.service;

import com.example.hr.models.Department;
import com.example.hr.models.User;
import com.example.hr.enums.UserStatus;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuthUserHelper authUserHelper;
    private final AuditEncryptionService auditEncryptionService;

    public DepartmentService(DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             AuthUserHelper authUserHelper,
                             AuditEncryptionService auditEncryptionService) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.authUserHelper = authUserHelper;
        this.auditEncryptionService = auditEncryptionService;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "'all'")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Integer id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id: " + id));
    }

    @CacheEvict(value = "departments", allEntries = true)
    public Department saveDepartment(Department dept) {
        return saveDepartment(dept, null);
    }

    @CacheEvict(value = "departments", allEntries = true)
    public Department saveDepartment(Department dept, Authentication authentication) {
        LocalDateTime now = LocalDateTime.now();
        User currentUser = authUserHelper.getCurrentUser(authentication);
        String encryptedActor = encryptActor(currentUser);
        Department target;

        if (dept.getId() != null) {
            target = getDepartmentById(dept.getId());
            target.setDepartmentName(dept.getDepartmentName());
            target.setPhoneNumber(dept.getPhoneNumber());
            target.setManager(dept.getManager());
            target.setWorkingHours(dept.getWorkingHours());
        } else {
            target = dept;
            target.setCreatedAt(now);
            target.setCreatedByEncrypted(encryptedActor);
        }

        target.setUpdatedAt(now);
        target.setUpdatedByEncrypted(encryptedActor);
        target.setEmployeeCount(calculateEmployeeCount(target));

        Department saved = departmentRepository.save(target);
        int employeeCount = calculateEmployeeCount(saved);
        if (saved.getEmployeeCount() == null || saved.getEmployeeCount() != employeeCount) {
            saved.setEmployeeCount(employeeCount);
            saved.setUpdatedAt(now);
            return departmentRepository.save(saved);
        }

        return saved;
    }

    @CacheEvict(value = "departments", allEntries = true)
    public void deleteDepartment(Integer id) {
        Department dept = getDepartmentById(id);
        if (calculateEmployeeCount(dept) > 0) {
            throw new IllegalStateException("Không thể xóa phòng ban đang có nhân sự.");
        }
        departmentRepository.deleteById(id);
    }

    private String encryptActor(User user) {
        if (user == null) {
            return auditEncryptionService.encrypt("Không xác định");
        }

        String actor = String.format("%s | %s | %s",
                user.getUsername(),
                user.getFullName(),
                user.getRole() != null ? user.getRole().name() : "NO_ROLE");
        return auditEncryptionService.encrypt(actor);
    }

    private int calculateEmployeeCount(Department department) {
        if (department == null || department.getId() == null) {
            return 0;
        }
        return (int) userRepository.countByDepartmentAndStatus(department, UserStatus.ACTIVE);
    }
}
