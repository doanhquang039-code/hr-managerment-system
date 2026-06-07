package com.example.hr.specification;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.OvertimeStatus;
import com.example.hr.models.OvertimeRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specification cho dynamic query OT.
 */
public final class OvertimeSpecification {

    private OvertimeSpecification() {}

    public static Specification<OvertimeRequest> hasUser(Integer userId) {
        return (root, query, cb) -> {
            if (userId == null) return cb.conjunction();
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<OvertimeRequest> hasStatus(OvertimeStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<OvertimeRequest> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) {
                return cb.between(root.get("overtimeDate"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("overtimeDate"), start);
            }
            return cb.lessThanOrEqualTo(root.get("overtimeDate"), end);
        };
    }

    public static Specification<OvertimeRequest> hasDepartment(Integer departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            return cb.equal(root.get("user").get("department").get("id"), departmentId);
        };
    }
}
