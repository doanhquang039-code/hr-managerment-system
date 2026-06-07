package com.example.hr.service;

import com.example.hr.enums.GroupFeature;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.CollaborationGroup;
import com.example.hr.models.CollaborationGroupRolePermission;
import com.example.hr.models.User;
import com.example.hr.repository.CollaborationGroupRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("groupAccessService")
@Transactional
public class GroupAccessService {

    public static final String DEFAULT_GROUP_NAME = "HR Collaboration Group";

    private final CollaborationGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuthUserHelper authUserHelper;

    public GroupAccessService(CollaborationGroupRepository groupRepository,
                              UserRepository userRepository,
                              AuthUserHelper authUserHelper) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.authUserHelper = authUserHelper;
    }

    public CollaborationGroup getDefaultGroup() {
        return groupRepository.findByName(DEFAULT_GROUP_NAME).orElseGet(this::createDefaultGroup);
    }

    @Transactional(readOnly = true)
    public List<User> getAssignableUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }

    public CollaborationGroup updateDefaultGroup(Set<Integer> memberIds, Set<String> rolePermissionKeys) {
        CollaborationGroup group = getDefaultGroup();
        Set<User> members = new HashSet<>(userRepository.findAllById(memberIds != null ? memberIds : Set.of()));
        group.setMembers(members);

        Set<CollaborationGroupRolePermission> permissions = parseRolePermissions(group, rolePermissionKeys);
        if (permissions.isEmpty()) {
            permissions = parseRolePermissions(group, Set.of(permissionKey(Role.ADMIN, GroupFeature.DASHBOARD)));
        }

        Set<String> desiredKeys = permissions.stream()
                .map(permission -> permissionKey(permission.getRole(), permission.getFeature()))
                .collect(Collectors.toSet());

        group.getRolePermissions().removeIf(existing ->
                !desiredKeys.contains(permissionKey(existing.getRole(), existing.getFeature())));

        Set<String> existingKeys = group.getRolePermissions().stream()
                .map(permission -> permissionKey(permission.getRole(), permission.getFeature()))
                .collect(Collectors.toSet());

        permissions.stream()
                .filter(permission -> !existingKeys.contains(permissionKey(permission.getRole(), permission.getFeature())))
                .forEach(group.getRolePermissions()::add);

        group.setRoles(permissions.stream().map(CollaborationGroupRolePermission::getRole).collect(Collectors.toSet()));
        group.setFeatures(permissions.stream().map(CollaborationGroupRolePermission::getFeature).collect(Collectors.toSet()));
        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserMember() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return groupRepository.existsByActiveTrueAndMembersContaining(currentUser);
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentUserAccess() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        CollaborationGroup group = getDefaultGroup();
        return group.isActive()
                && (hasAnyRolePermission(group, currentUser.getRole()) || isExplicitMember(group, currentUser));
    }

    @Transactional(readOnly = true)
    public boolean hasFeature(String featureName) {
        GroupFeature feature = parseFeature(featureName);
        if (feature == null) {
            return false;
        }
        CollaborationGroup group = getDefaultGroup();
        User currentUser = getCurrentUser();
        if (currentUser == null || !group.isActive()) {
            return false;
        }
        if (hasRolePermission(group, currentUser.getRole(), feature)) {
            return true;
        }
        return isExplicitMember(group, currentUser) && group.getFeatures().contains(feature);
    }

    public GroupFeature[] getAllFeatures() {
        return GroupFeature.values();
    }

    public Role[] getAllRoles() {
        return Role.values();
    }

    public Set<String> getEnabledPermissionKeys(CollaborationGroup group) {
        return group.getRolePermissions().stream()
                .map(permission -> permissionKey(permission.getRole(), permission.getFeature()))
                .collect(Collectors.toSet());
    }

    private CollaborationGroup createDefaultGroup() {
        CollaborationGroup group = new CollaborationGroup();
        group.setName(DEFAULT_GROUP_NAME);
        group.setDescription("Shared group with role-based feature permissions.");
        Set<String> defaultKeys = Arrays.stream(Role.values())
                .flatMap(role -> Arrays.stream(GroupFeature.values())
                        .map(feature -> permissionKey(role, feature)))
                .collect(Collectors.toSet());
        Set<CollaborationGroupRolePermission> permissions = parseRolePermissions(group, defaultKeys);
        group.getRolePermissions().addAll(permissions);
        group.setRoles(permissions.stream().map(CollaborationGroupRolePermission::getRole).collect(Collectors.toSet()));
        group.setFeatures(permissions.stream().map(CollaborationGroupRolePermission::getFeature).collect(Collectors.toSet()));
        group.setCreatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authUserHelper.getCurrentUser(authentication);
    }

    private boolean isExplicitMember(CollaborationGroup group, User currentUser) {
        return group.getMembers().stream()
                .anyMatch(member -> member.getId() != null && member.getId().equals(currentUser.getId()));
    }

    private boolean hasAnyRolePermission(CollaborationGroup group, Role role) {
        if (role == null) {
            return false;
        }
        return group.getRolePermissions().stream()
                .anyMatch(permission -> permission.getRole() == role);
    }

    private boolean hasRolePermission(CollaborationGroup group, Role role, GroupFeature feature) {
        if (role == null || feature == null) {
            return false;
        }
        return group.getRolePermissions().stream()
                .anyMatch(permission -> permission.getRole() == role && permission.getFeature() == feature);
    }

    private Set<CollaborationGroupRolePermission> parseRolePermissions(CollaborationGroup group, Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashSet<>();
        }
        Set<CollaborationGroupRolePermission> permissions = new HashSet<>();
        for (String key : keys) {
            String[] parts = key != null ? key.split(":", 2) : new String[0];
            if (parts.length != 2) {
                continue;
            }
            try {
                Role role = Role.valueOf(parts[0]);
                GroupFeature feature = GroupFeature.valueOf(parts[1]);
                CollaborationGroupRolePermission permission = new CollaborationGroupRolePermission();
                permission.setGroup(group);
                permission.setRole(role);
                permission.setFeature(feature);
                permissions.add(permission);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed permission values from the form.
            }
        }
        return permissions;
    }

    private static String permissionKey(Role role, GroupFeature feature) {
        return role.name() + ":" + feature.name();
    }

    private GroupFeature parseFeature(String featureName) {
        if (featureName == null || featureName.isBlank()) {
            return null;
        }
        try {
            return GroupFeature.valueOf(featureName.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}


