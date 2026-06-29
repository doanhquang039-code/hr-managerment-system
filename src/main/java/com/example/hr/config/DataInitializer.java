package com.example.hr.config;

import com.example.hr.recruitment.repository.JobPostingRepository;
import com.example.hr.recruitment.entity.JobPosting;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.department.entity.Department;
import java.time.LocalDate;


import com.example.hr.service.GroupRoleService;
import com.example.hr.service.GroupAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;


/**
 * Runs after application startup to seed required data.
 * Seeds default GroupRoles and migrates legacy data (handling 0 values created by ddl-auto).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final GroupRoleService groupRoleService;
    private final GroupAccessService groupAccessService;
    private final JdbcTemplate jdbcTemplate;
    private final com.example.hr.repository.CustomGroupFeatureRepository customGroupFeatureRepository;
    private final JobPostingRepository jobPostingRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 0. Seed custom features
            try {
                var customFeatures = customGroupFeatureRepository.findAll();
                for (var f : customFeatures) {
                    com.example.hr.enums.GroupFeature.register(f.getName(), f.getDisplayName());
                }
                log.info("[DataInitializer] Custom features loaded: " + customFeatures.size());
            } catch (Exception e) {
                log.warn("[DataInitializer] Dynamic group features load failed: " + e.getMessage());
            }

            // 1. Seed dynamic roles if empty
            groupRoleService.seedDefaultRolesIfEmpty();
            log.info("[DataInitializer] GroupRole seed completed.");

            // 2. Ensure default group exists
            var defaultGroup = groupAccessService.getDefaultGroup();

            // 3. Migrate legacy user roles from `role` column to `group_role_id`
            try {
                jdbcTemplate.execute(
                    "UPDATE user SET group_role_id = (SELECT id FROM group_roles WHERE name = role) " +
                    "WHERE group_role_id IS NULL OR group_role_id = 0"
                );
                // Set any remaining invalid '0' ids to NULL
                jdbcTemplate.execute("UPDATE user SET group_role_id = NULL WHERE group_role_id = 0");
                log.info("[DataInitializer] Migrated legacy user roles to group_role_id.");
            } catch (Exception e) {
                log.warn("[DataInitializer] User role migration warning: {}", e.getMessage());
            }

            // 4. Migrate legacy group role permissions from `role` column to `group_role_id`
            try {
                jdbcTemplate.execute(
                    "UPDATE collaboration_group_role_permissions SET group_role_id = (SELECT id FROM group_roles WHERE name = role) " +
                    "WHERE group_role_id IS NULL OR group_role_id = 0"
                );
                // Delete any remaining invalid '0' or NULL rows in permissions table to prevent entity resolving crash
                jdbcTemplate.execute("DELETE FROM collaboration_group_role_permissions WHERE group_role_id = 0 OR group_role_id IS NULL");
                log.info("[DataInitializer] Migrated legacy group role permissions to group_role_id.");
            } catch (Exception e) {
                log.warn("[DataInitializer] Group role permissions migration warning: {}", e.getMessage());
            }

            // 4.5 Seed default active Job Postings if empty
            try {
                if (jobPostingRepository.count() == 0) {
                    List<Department> depts = departmentRepository.findAll();
                    Department hrDept = depts.stream().filter(d -> d.getDepartmentName().contains("Nhân Sự")).findFirst().orElse(null);
                    Department techDept = depts.stream().filter(d -> d.getDepartmentName().contains("Kỹ Thuật")).findFirst().orElse(null);
                    Department salesDept = depts.stream().filter(d -> d.getDepartmentName().contains("Kinh Doanh")).findFirst().orElse(null);

                    if (hrDept == null && !depts.isEmpty()) {
                        hrDept = depts.get(0);
                    }
                    if (techDept == null && !depts.isEmpty()) {
                        techDept = depts.size() > 1 ? depts.get(1) : depts.get(0);
                    }
                    if (salesDept == null && !depts.isEmpty()) {
                        salesDept = depts.size() > 2 ? depts.get(2) : depts.get(0);
                    }

                    // Seeding Java Developer
                    JobPosting javaDev = new JobPosting();
                    javaDev.setTitle("Lập Trình Viên Java (Spring Boot)");
                    javaDev.setDescription("Phát triển các ứng dụng backend chất lượng cao sử dụng Spring Boot, Hibernate và MySQL.");
                    javaDev.setRequirements("Có từ 2 năm kinh nghiệm làm việc với Java/Spring Boot. Hiểu biết tốt về RESTful API và Hibernate.");
                    javaDev.setDepartment(techDept);
                    javaDev.setEmploymentType("FULL_TIME");
                    javaDev.setExperienceLevel("MID");
                    javaDev.setSalaryMin(new java.math.BigDecimal("15000000"));
                    javaDev.setSalaryMax(new java.math.BigDecimal("30000000"));
                    javaDev.setLocation("TP. Hồ Chí Minh");
                    javaDev.setRemoteAllowed(true);
                    javaDev.setPostingDate(LocalDate.now());
                    javaDev.setClosingDate(LocalDate.now().plusMonths(3));
                    javaDev.setStatus("ACTIVE");
                    jobPostingRepository.save(javaDev);

                    // Seeding Frontend Developer
                    JobPosting feDev = new JobPosting();
                    feDev.setTitle("Lập Trình Viên Frontend (ReactJS)");
                    feDev.setDescription("Xây dựng giao diện ứng dụng web tối ưu, hiện đại, mượt mà sử dụng ReactJS.");
                    feDev.setRequirements("Thành thạo ReactJS, Javascript (ES6), HTML5/CSS3. Ưu tiên có kinh nghiệm về Tailwind CSS.");
                    feDev.setDepartment(techDept);
                    feDev.setEmploymentType("FULL_TIME");
                    feDev.setExperienceLevel("MID");
                    feDev.setSalaryMin(new java.math.BigDecimal("12000000"));
                    feDev.setSalaryMax(new java.math.BigDecimal("25000000"));
                    feDev.setLocation("Hà Nội");
                    feDev.setRemoteAllowed(false);
                    feDev.setPostingDate(LocalDate.now());
                    feDev.setClosingDate(LocalDate.now().plusMonths(2));
                    feDev.setStatus("ACTIVE");
                    jobPostingRepository.save(feDev);

                    // Seeding HR Specialist
                    JobPosting hrSpec = new JobPosting();
                    hrSpec.setTitle("Chuyên Viên Tuyển Dụng Nhân Sự");
                    hrSpec.setDescription("Tìm kiếm, tuyển chọn và đồng hành cùng các tài năng gia nhập công ty.");
                    hrSpec.setRequirements("Có từ 1 năm kinh nghiệm tuyển dụng. Kỹ năng giao tiếp xuất sắc và nhạy bén với nhân tài.");
                    hrSpec.setDepartment(hrDept);
                    hrSpec.setEmploymentType("FULL_TIME");
                    hrSpec.setExperienceLevel("ENTRY");
                    hrSpec.setSalaryMin(new java.math.BigDecimal("10000000"));
                    hrSpec.setSalaryMax(new java.math.BigDecimal("18000000"));
                    hrSpec.setLocation("TP. Hồ Chí Minh");
                    hrSpec.setRemoteAllowed(true);
                    hrSpec.setPostingDate(LocalDate.now());
                    hrSpec.setClosingDate(LocalDate.now().plusMonths(1));
                    hrSpec.setStatus("ACTIVE");
                    jobPostingRepository.save(hrSpec);

                    log.info("[DataInitializer] Seeded 3 default active Job Postings.");
                }
            } catch (Exception e) {
                log.warn("[DataInitializer] Job posting seeding warning: {}", e.getMessage());
            }

            // 5. Seed default permissions for the default group
            try {
                List<Integer> roleIds = jdbcTemplate.queryForList("SELECT id FROM group_roles", Integer.class);
                int countSeeded = 0;
                for (Integer roleId : roleIds) {
                    for (String feature : java.util.List.of(
                            "DASHBOARD", "MEMBERS", "NOTES", "TASKS", "FILES", "MEETINGS", "ANNOUNCEMENTS", "RECOGNITION"
                    )) {
                        Integer exists = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM collaboration_group_role_permissions WHERE group_id = ? AND group_role_id = ? AND feature = ?",
                            Integer.class,
                            defaultGroup.getId(), roleId, feature
                        );
                        if (exists == null || exists == 0) {
                            jdbcTemplate.update(
                                "INSERT INTO collaboration_group_role_permissions (group_id, group_role_id, feature) VALUES (?, ?, ?)",
                                defaultGroup.getId(), roleId, feature
                            );
                            countSeeded++;
                        }
                    }
                }
                if (countSeeded > 0) {
                    log.info("[DataInitializer] Seeded {} missing default permissions for default group.", countSeeded);
                }
            } catch (Exception e) {
                log.warn("[DataInitializer] Default group permissions seeding warning: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.warn("[DataInitializer] Seed or migration failed: {}", e.getMessage());
        }
    }
}
