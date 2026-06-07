package com.example.hr.user.service;

import com.example.hr.enums.UserStatus;
import com.example.hr.models.User;
import com.example.hr.department.entity.Department;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.recruitment.repository.JobPositionRepository;
import com.example.hr.service.AuditEncryptionService;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.CloudinaryService;
import com.example.hr.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final AuthUserHelper authUserHelper;
    private final AuditEncryptionService auditEncryptionService;
 
    public UserService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            JobPositionRepository positionRepository,
            PasswordEncoder passwordEncoder,
            CloudinaryService cloudinaryService,
            AuthUserHelper authUserHelper,
            AuditEncryptionService auditEncryptionService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
        this.authUserHelper = authUserHelper;
        this.auditEncryptionService = auditEncryptionService;
    }

    @CacheEvict(value = {"users", "departments"}, allEntries = true)
    public void registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        refreshDepartmentEmployeeCount(saved.getDepartment());
    }
    
    // Get user by username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'active'")
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<User> findAdminUsers(String keyword, Integer deptId, Integer positionId, String role, org.springframework.data.domain.Pageable pageable) {
        com.example.hr.enums.Role roleEnum = null;
        if (role != null && !role.isBlank()) {
            try {
                roleEnum = com.example.hr.enums.Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                // Ignore invalid role
            }
        }
        return userRepository.searchActiveUsersForAdmin(keyword, deptId, positionId, roleEnum, pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<User> searchUsers(String keyword, org.springframework.data.domain.Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @CacheEvict(value = {"users", "departments"}, allEntries = true)
    public User saveAdminUser(User user,
                              MultipartFile file,
                              Integer departmentId,
                              Integer positionId,
                              String phoneNumber,
                              String gender,
                              String dateOfBirth,
                              String address,
                              String employeeCode,
                              String cccd,
                              String hireDate) throws IOException {
        return saveAdminUser(user, file, departmentId, positionId, phoneNumber, gender, dateOfBirth,
                address, employeeCode, cccd, hireDate, null);
    }

    @CacheEvict(value = {"users", "departments"}, allEntries = true)
    public User saveAdminUser(User user,
                              MultipartFile file,
                              Integer departmentId,
                              Integer positionId,
                              String phoneNumber,
                              String gender,
                              String dateOfBirth,
                              String address,
                              String employeeCode,
                              String cccd,
                              String hireDate,
                              Authentication authentication) throws IOException {
        User existing = user.getId() != null ? getUserById(user.getId()) : null;
        Department oldDepartment = existing != null ? existing.getDepartment() : null;

        applyRelations(user, departmentId, positionId);
        applyEditableFields(user, phoneNumber, gender, dateOfBirth, address, employeeCode, cccd, hireDate, existing);
        handleProfileImage(user, file, existing);
        handlePassword(user, existing);
        touchAuditFields(user, existing, authentication);

        User saved = userRepository.save(user);
        refreshDepartmentEmployeeCount(oldDepartment);
        refreshDepartmentEmployeeCount(saved.getDepartment());
        return saved;
    }

    @CacheEvict(value = {"users", "departments"}, allEntries = true)
    public void softDeleteUser(Integer id) {
        User user = getUserById(id);
        Department oldDepartment = user.getDepartment();
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        refreshDepartmentEmployeeCount(oldDepartment);
    }

    private void applyRelations(User user, Integer departmentId, Integer positionId) {
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(user::setDepartment);
        } else {
            user.setDepartment(null);
        }

        if (positionId != null) {
            positionRepository.findById(positionId).ifPresent(user::setPosition);
        } else {
            user.setPosition(null);
        }
    }

    private void applyEditableFields(User user,
                                     String phoneNumber,
                                     String gender,
                                     String dateOfBirth,
                                     String address,
                                     String employeeCode,
                                     String cccd,
                                     String hireDate,
                                     User existing) {
        user.setPhoneNumber(phoneNumber);
        user.setGender(gender);
        user.setAddress(address);
        if (cccd != null && !cccd.isBlank()) {
            user.setCccd(cccd.trim());
        } else if (existing != null) {
            user.setCccd(existing.getCccd());
        }
        
        // Handle employee code - generate unique if not provided or if creating new user
        if (existing == null) {
            // Creating new user
            if (employeeCode != null && !employeeCode.isBlank()) {
                // Check if provided code already exists
                if (userRepository.findByEmployeeCode(employeeCode).isPresent()) {
                    // Generate new unique code
                    user.setEmployeeCode(generateUniqueEmployeeCode());
                } else {
                    user.setEmployeeCode(employeeCode);
                }
            } else {
                // No code provided, generate one
                user.setEmployeeCode(generateUniqueEmployeeCode());
            }
        } else {
            // Updating existing user
            if (employeeCode != null && !employeeCode.isBlank()) {
                // Check if code changed and if new code already exists
                if (!employeeCode.equals(existing.getEmployeeCode())) {
                    if (userRepository.findByEmployeeCode(employeeCode).isPresent()) {
                        // Keep old code if new one is duplicate
                        user.setEmployeeCode(existing.getEmployeeCode());
                    } else {
                        user.setEmployeeCode(employeeCode);
                    }
                } else {
                    user.setEmployeeCode(employeeCode);
                }
            } else {
                // Keep existing code
                user.setEmployeeCode(existing.getEmployeeCode());
            }
        }
        
        user.setDateOfBirth(parseDate(dateOfBirth, existing != null ? existing.getDateOfBirth() : null));
        user.setHireDate(parseDate(hireDate, existing != null ? existing.getHireDate() : null));

        if (existing != null && user.getCreatedAt() == null) {
            user.setCreatedAt(existing.getCreatedAt());
        }
        if (existing != null && user.getStatus() == null) {
            user.setStatus(existing.getStatus());
        }
    }
    
    /**
     * Generate unique employee code in format NV00001, NV00002, etc.
     */
    private String generateUniqueEmployeeCode() {
        // Find the highest employee code
        List<User> allUsers = userRepository.findAll();
        int maxNumber = 0;
        
        for (User u : allUsers) {
            String code = u.getEmployeeCode();
            if (code != null && code.startsWith("NV")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Skip invalid codes
                }
            }
        }
        
        // Generate next code
        int nextNumber = maxNumber + 1;
        String newCode = String.format("NV%05d", nextNumber);
        
        // Double check it doesn't exist (safety check)
        while (userRepository.findByEmployeeCode(newCode).isPresent()) {
            nextNumber++;
            newCode = String.format("NV%05d", nextNumber);
        }
        
        return newCode;
    }

    private void handleProfileImage(User user, MultipartFile file, User existing) throws IOException {
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                Map<?, ?> result = cloudinaryService.uploadAvatar(file, "hr_avatars");
                user.setProfileImage(result.get("secure_url").toString());
                return;
            }
        }

        if (existing != null) {
            user.setProfileImage(existing.getProfileImage());
        }
    }

    private void handlePassword(User user, User existing) {
        if (existing == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return;
        }

        String rawPwd = user.getPassword();
        if (rawPwd != null && !rawPwd.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPwd));
        } else {
            user.setPassword(existing.getPassword());
        }
    }

    private void touchAuditFields(User user, User existing, Authentication authentication) {
        LocalDateTime now = LocalDateTime.now();
        boolean creating = existing == null;
        String encryptedActor = encryptActor(authUserHelper.getCurrentUser(authentication));
        if (creating && user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        if (creating) {
            user.setCreatedByEncrypted(encryptedActor);
        } else {
            user.setCreatedAt(existing.getCreatedAt());
            user.setCreatedByEncrypted(existing.getCreatedByEncrypted());
        }
        user.setUpdatedByEncrypted(encryptedActor);
        user.setUpdatedAt(now);
    }

    private String encryptActor(User actor) {
        if (actor == null) {
            return auditEncryptionService.encrypt("Khong xac dinh");
        }
        String payload = String.format("%s | %s | %s",
                actor.getUsername(),
                actor.getFullName(),
                actor.getRole() != null ? actor.getRole().name() : "NO_ROLE");
        return auditEncryptionService.encrypt(payload);
    }

    private boolean matchesKeyword(User user, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String kw = keyword.toLowerCase();
        return user.getFullName().toLowerCase().contains(kw)
                || (user.getEmail() != null && user.getEmail().toLowerCase().contains(kw))
                || (user.getEmployeeCode() != null && user.getEmployeeCode().toLowerCase().contains(kw));
    }

    private boolean matchesDepartment(User user, Integer deptId) {
        return deptId == null || (user.getDepartment() != null && user.getDepartment().getId().equals(deptId));
    }

    private boolean matchesRole(User user, String role) {
        return role == null || role.isBlank() || (user.getRole() != null && user.getRole().name().equals(role));
    }

    private Comparator<User> resolveComparator(String sortBy) {
        if ("createdAt".equals(sortBy)) {
            return Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        if ("department".equals(sortBy)) {
            return Comparator.comparing(
                    user -> user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "",
                    String.CASE_INSENSITIVE_ORDER
            );
        }
        return Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER);
    }

    private LocalDate parseDate(String rawDate, LocalDate fallback) {
        if (rawDate == null || rawDate.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void refreshDepartmentEmployeeCount(Department department) {
        if (department == null || department.getId() == null) {
            return;
        }

        Department managedDepartment = departmentRepository.findById(department.getId()).orElse(null);
        if (managedDepartment == null) {
            return;
        }

        managedDepartment.setEmployeeCount((int) userRepository.countByDepartmentAndStatus(managedDepartment, UserStatus.ACTIVE));
        managedDepartment.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(managedDepartment);
    }

    /**
     * Get users by role for hiring module
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleName) {
        return getActiveUsers().stream()
                .filter(user -> user.getRole() != null && user.getRole().name().equals(roleName))
                .toList();
    }

    /**
     * Get all users (including inactive) for admin management
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by department for manager tools
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByDepartment(Department department) {
        return userRepository.findByDepartment(department);
    }
}
