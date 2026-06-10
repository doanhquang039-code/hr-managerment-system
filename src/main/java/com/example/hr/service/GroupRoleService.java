package com.example.hr.service;

import com.example.hr.models.GroupRole;
import com.example.hr.repository.GroupRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CRUD service for GroupRole — the dynamic replacement for the hardcoded Role enum.
 */
@Service
@RequiredArgsConstructor
public class GroupRoleService {

    private final GroupRoleRepository groupRoleRepository;

    @Transactional(readOnly = true)
    public List<GroupRole> findAll() {
        return groupRoleRepository.findAllByOrderBySortOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<GroupRole> findById(Integer id) {
        return groupRoleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<GroupRole> findByName(String name) {
        return groupRoleRepository.findByName(name);
    }

    /**
     * Create a new role.
     * @throws IllegalArgumentException if name already exists
     */
    @Transactional
    public GroupRole create(String name, String displayName, String color, String textColor, int sortOrder) {
        String normalized = name.trim().toUpperCase().replaceAll("\\s+", "_");
        if (groupRoleRepository.existsByName(normalized)) {
            throw new IllegalArgumentException("Role '" + normalized + "' đã tồn tại.");
        }
        GroupRole role = GroupRole.builder()
                .name(normalized)
                .displayName(displayName)
                .color(color != null ? color : "#e2e8f0")
                .textColor(textColor != null ? textColor : "#334155")
                .sortOrder(sortOrder)
                .builtIn(false)
                .createdAt(LocalDateTime.now())
                .build();
        return groupRoleRepository.save(role);
    }

    /**
     * Update an existing role.
     * Cannot change the name of a built-in role.
     */
    @Transactional
    public GroupRole update(Integer id, String displayName, String color, String textColor, int sortOrder) {
        GroupRole role = groupRoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role id=" + id));
        role.setDisplayName(displayName);
        role.setColor(color != null ? color : role.getColor());
        role.setTextColor(textColor != null ? textColor : role.getTextColor());
        role.setSortOrder(sortOrder);
        return groupRoleRepository.save(role);
    }

    /**
     * Delete a role.
     * @throws IllegalStateException if role is built-in or still assigned to users
     */
    @Transactional
    public void delete(Integer id) {
        GroupRole role = groupRoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role id=" + id));

        if (role.isBuiltIn()) {
            throw new IllegalStateException(
                    "Role '" + role.getDisplayName() + "' là role tích hợp, không thể xóa.");
        }

        long userCount = groupRoleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new IllegalStateException(
                    "Không thể xóa role '" + role.getDisplayName() + "' vì đang có " +
                    userCount + " người dùng sử dụng. Vui lòng chuyển họ sang role khác trước.");
        }

        groupRoleRepository.delete(role);
    }

    /**
     * Seed default roles from the old Role enum if group_roles table is empty.
     * Called from application startup.
     */
    @Transactional
    public void seedDefaultRolesIfEmpty() {
        if (groupRoleRepository.count() > 0) return;

        Object[][] defaults = {
            {"ADMIN",    "Quản trị viên",        "#fee2e2", "#991b1b", 0, true},
            {"MANAGER",  "Quản lý",              "#fef9c3", "#854d0e", 1, true},
            {"HIRING",   "Nhân sự/Tuyển dụng",  "#f0fdf4", "#166534", 2, true},
            {"USER",     "Nhân viên",            "#eff6ff", "#1d4ed8", 3, true},
        };

        for (Object[] d : defaults) {
            GroupRole role = GroupRole.builder()
                    .name((String) d[0])
                    .displayName((String) d[1])
                    .color((String) d[2])
                    .textColor((String) d[3])
                    .sortOrder((int) d[4])
                    .builtIn((boolean) d[5])
                    .createdAt(LocalDateTime.now())
                    .build();
            groupRoleRepository.save(role);
        }
    }
}
