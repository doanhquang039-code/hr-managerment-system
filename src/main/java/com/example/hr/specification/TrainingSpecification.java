package com.example.hr.specification;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.TrainingStatus;
import com.example.hr.training.entity.TrainingProgram;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specification cho dynamic query training programs.
 */
public final class TrainingSpecification {

    private TrainingSpecification() {}

    public static Specification<TrainingProgram> hasStatus(TrainingStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<TrainingProgram> hasDepartment(Integer departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            return cb.equal(root.get("department").get("id"), departmentId);
        };
    }

    public static Specification<TrainingProgram> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("programName")), pattern);
        };
    }

    public static Specification<TrainingProgram> startDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) {
                return cb.between(root.get("startDate"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("startDate"), start);
            }
            return cb.lessThanOrEqualTo(root.get("startDate"), end);
        };
    }

    public static Specification<TrainingProgram> hasTrainingType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return cb.conjunction();
            return cb.equal(root.get("trainingType"), type);
        };
    }
}

