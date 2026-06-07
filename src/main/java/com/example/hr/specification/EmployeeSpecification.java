package com.example.hr.specification;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification cho dynamic query nhân viên.
 */
public final class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<User> hasDepartment(Integer departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            return cb.equal(root.get("department").get("id"), departmentId);
        };
    }

    public static Specification<User> hasPosition(Integer positionId) {
        return (root, query, cb) -> {
            if (positionId == null) return cb.conjunction();
            return cb.equal(root.get("position").get("id"), positionId);
        };
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<User> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

    public static Specification<User> hasRole(com.example.hr.enums.Role role) {
        return (root, query, cb) -> {
            if (role == null) return cb.conjunction();
            return cb.equal(root.get("role"), role);
        };
    }
}
