package com.example.hr.config;

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
