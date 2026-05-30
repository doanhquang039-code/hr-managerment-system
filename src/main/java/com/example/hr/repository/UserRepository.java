package com.example.hr.repository;

import java.util.List;
import java.util.Optional;

import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Hàm dùng cho Login
    Optional<User> findByEmail(String email);

    // Hàm dùng cho các Controller khác (Hiring, Contract, Task...)
    Optional<User> findByUsername(String username);

    // Hàm lấy danh sách theo trạng thái (Hết lỗi findByStatus)
    List<User> findByStatus(UserStatus status);

    // Hàm tìm kiếm theo tên (Hết lỗi findByFullNameContainingAndStatus)
    List<User> findByFullNameContainingAndStatus(String fullName, UserStatus status);
    org.springframework.data.domain.Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, org.springframework.data.domain.Pageable pageable);
    
    // Lọc theo phòng ban và sắp xếp
    List<User> findByDepartmentId(Integer deptId, Sort sort);

    List<User> findByRoleInAndStatus(List<Role> roles, UserStatus status);

    // UserApiController methods
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:deptId IS NULL OR u.department.id = :deptId) AND " +
           "(:positionId IS NULL OR u.position.id = :positionId) AND " +
           "(:roleEnum IS NULL OR u.role = :roleEnum)")
    org.springframework.data.domain.Page<User> searchActiveUsersForAdmin(
        @Param("keyword") String keyword, 
        @Param("deptId") Integer deptId, 
        @Param("positionId") Integer positionId,
        @Param("roleEnum") com.example.hr.enums.Role roleEnum, 
        org.springframework.data.domain.Pageable pageable);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByUsernameAndIdNot(String username, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Integer id);
    
    // Find by employee code
    Optional<User> findByEmployeeCode(String employeeCode);

    List<User> findByStatusAndFullNameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCaseOrStatusAndEmployeeCodeContainingIgnoreCase(
            UserStatus s1, String name,
            UserStatus s2, String email,
            UserStatus s3, String code);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    long countByStatus(UserStatus status);
    long countByDepartmentAndStatus(com.example.hr.models.Department department, UserStatus status);

    @Query("""
            SELECT COALESCE(d.departmentName, 'Unassigned'), COUNT(u)
            FROM User u
            LEFT JOIN u.department d
            WHERE u.status = :status
            GROUP BY d.departmentName
            """)
    List<Object[]> countByDepartmentForStatus(@Param("status") UserStatus status);
    
    // Advanced Analytics methods
    long countByCreatedAtAfter(LocalDateTime createdAt);
    long countByCreatedAtBefore(LocalDateTime createdAt);
    long countByDepartment(com.example.hr.models.Department department);
    List<User> findByDepartmentAndStatus(com.example.hr.models.Department department, UserStatus status);
    List<User> findTop5ByOrderByCreatedAtDesc();

    // Additional method for manager tools
    List<User> findByDepartment(com.example.hr.models.Department department);

    @Query("SELECT u FROM User u WHERE u.department = :department AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:roleEnum IS NULL OR u.role = :roleEnum)")
    List<User> searchDepartmentMembers(
        @Param("department") com.example.hr.models.Department department,
        @Param("keyword") String keyword, 
        @Param("roleEnum") com.example.hr.enums.Role roleEnum);
}
