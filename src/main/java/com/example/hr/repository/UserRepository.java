package com.example.hr.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
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
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Sort sort);
    
    // Lọc theo phòng ban và sắp xếp
    List<User> findByDepartmentId(Integer deptId, Sort sort);

    List<User> findByRoleInAndStatus(List<Role> roles, UserStatus status);
}