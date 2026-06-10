package com.example.hr.service;

import com.example.hr.enums.GroupFeature;
import com.example.hr.enums.NotificationType;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.*;
import com.example.hr.repository.CollaborationGroupRepository;
import com.example.hr.repository.GroupRoleRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("groupAccessService")
@Transactional
public class GroupAccessService {

    public static final String DEFAULT_GROUP_NAME = "HR Collaboration Group";

    private final CollaborationGroupRepository groupRepository;
    private final GroupRoleRepository groupRoleRepository;
    private final UserRepository userRepository;
    private final AuthUserHelper authUserHelper;
    private final NotificationService notificationService;
    private final HrAuditLogService hrAuditLogService;

    public GroupAccessService(CollaborationGroupRepository groupRepository,
                              GroupRoleRepository groupRoleRepository,
                              UserRepository userRepository,
                              AuthUserHelper authUserHelper,
                              NotificationService notificationService,
                              HrAuditLogService hrAuditLogService) {
        this.groupRepository = groupRepository;
        this.groupRoleRepository = groupRoleRepository;
        this.userRepository = userRepository;
        this.authUserHelper = authUserHelper;
        this.notificationService = notificationService;
        this.hrAuditLogService = hrAuditLogService;
    }

    // ─── Group CRUD ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CollaborationGroup> findAllGroups() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<CollaborationGroup> findGroupById(Integer id) {
        return groupRepository.findById(id);
    }

    public CollaborationGroup getDefaultGroup() {
        return groupRepository.findByName(DEFAULT_GROUP_NAME)
                .orElseGet(this::createDefaultGroup);
    }

    public CollaborationGroup createGroup(String name, String description, String iconClass) {
        CollaborationGroup group = new CollaborationGroup();
        group.setName(name);
        group.setDescription(description);
        group.setIconClass(iconClass != null ? iconClass : "bi-diagram-3");
        group.setActive(true);
        group.setCreatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    public CollaborationGroup updateGroupMeta(Integer groupId, String name, String description, String iconClass) {
        CollaborationGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy group id=" + groupId));
        group.setName(name);
        group.setDescription(description);
        group.setIconClass(iconClass != null ? iconClass : group.getIconClass());
        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    public void deleteGroup(Integer groupId) {
        CollaborationGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy group id=" + groupId));
        if (group.getName().equals(DEFAULT_GROUP_NAME)) {
            throw new IllegalStateException("Không thể xóa group mặc định hệ thống.");
        }
        groupRepository.delete(group);
    }

    // ─── Permission Update ──────────────────────────────────────────────────────

    /**
     * Save all permissions for a group.
     *
     * @param groupId           target group ID
     * @param memberIds         explicit member user IDs
     * @param rolePermissionKeys set of "roleId:FEATURE" keys
     * @param memberPermissionKeys set of "userId:FEATURE" keys
     */
    public CollaborationGroup updateGroupPermissions(Integer groupId,
                                                     Set<Integer> memberIds,
                                                     Set<String> rolePermissionKeys,
                                                     Set<String> memberPermissionKeys) {
        CollaborationGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy group id=" + groupId));

        Set<Integer> previousMemberIds = group.getMembers().stream()
                .map(User::getId).collect(Collectors.toSet());
        Set<String> previousMemberPermissionKeys = getEnabledMemberPermissionKeys(group);

        // Update explicit members
        Set<User> members = new HashSet<>(userRepository.findAllById(
                memberIds != null ? memberIds : Set.of()));
        group.setMembers(members);

        // Update role permissions
        Set<CollaborationGroupRolePermission> rolePerms = parseRolePermissions(group, rolePermissionKeys);
        Set<String> desiredRoleKeys = rolePerms.stream()
                .map(p -> permissionKey(p.getGroupRole(), p.getFeature()))
                .collect(Collectors.toSet());

        group.getRolePermissions().removeIf(existing ->
                !desiredRoleKeys.contains(permissionKey(existing.getGroupRole(), existing.getFeature())));

        Set<String> existingRoleKeys = group.getRolePermissions().stream()
                .map(p -> permissionKey(p.getGroupRole(), p.getFeature()))
                .collect(Collectors.toSet());

        rolePerms.stream()
                .filter(p -> !existingRoleKeys.contains(permissionKey(p.getGroupRole(), p.getFeature())))
                .forEach(group.getRolePermissions()::add);

        // Update member permissions
        Set<CollaborationGroupMemberPermission> memberPerms = parseMemberPermissions(group, memberPermissionKeys);
        Set<String> desiredMemberKeys = memberPerms.stream()
                .map(p -> permissionKey(p.getUser(), p.getFeature()))
                .collect(Collectors.toSet());

        group.getMemberPermissions().removeIf(existing ->
                !desiredMemberKeys.contains(permissionKey(existing.getUser(), existing.getFeature())));

        Set<String> existingMemberKeys = group.getMemberPermissions().stream()
                .map(p -> permissionKey(p.getUser(), p.getFeature()))
                .collect(Collectors.toSet());

        memberPerms.stream()
                .filter(p -> !existingMemberKeys.contains(permissionKey(p.getUser(), p.getFeature())))
                .forEach(group.getMemberPermissions()::add);

        group.setUpdatedAt(LocalDateTime.now());
        CollaborationGroup savedGroup = groupRepository.save(group);

        notifyNewAccess(previousMemberIds, previousMemberPermissionKeys, members, memberPerms);
        auditGroupPermissionChange(savedGroup, rolePermissionKeys, memberPermissionKeys);

        return savedGroup;
    }

    /** Backward-compat alias for single default group */
    public CollaborationGroup updateDefaultGroup(Set<Integer> memberIds,
                                                  Set<String> rolePermissionKeys,
                                                  Set<String> memberPermissionKeys) {
        return updateGroupPermissions(getDefaultGroup().getId(), memberIds, rolePermissionKeys, memberPermissionKeys);
    }

    public CollaborationGroup updateDefaultGroup(Set<Integer> memberIds, Set<String> rolePermissionKeys) {
        return updateDefaultGroup(memberIds, rolePermissionKeys, Set.of());
    }

    // ─── Query helpers ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<User> getAssignableUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<GroupRole> getAllRoles() {
        return groupRoleRepository.findAllByOrderBySortOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public List<User> getEffectiveMembers() {
        CollaborationGroup group = getDefaultGroup();
        Set<User> members = new LinkedHashSet<>();
        for (User user : getAssignableUsers()) {
            if (isEffectiveMember(group, user)) members.add(user);
        }
        return members.stream()
                .sorted(Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<User> getEffectiveMembersForGroup(Integer groupId) {
        CollaborationGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) return List.of();
        Set<User> members = new LinkedHashSet<>();
        for (User user : getAssignableUsers()) {
            if (isEffectiveMember(group, user)) members.add(user);
        }
        return members.stream()
                .sorted(Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canAssignToDefaultGroup(Integer userId) {
        if (userId == null) return true;
        CollaborationGroup group = getDefaultGroup();
        return userRepository.findById(userId)
                .map(user -> isEffectiveMember(group, user))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserMember() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        return groupRepository.existsByActiveTrueAndMembersContaining(currentUser);
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentUserAccess() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;
        if (isAdminRole(currentUser)) return true;
        CollaborationGroup group = getDefaultGroup();
        return group.isActive() && isEffectiveMember(group, currentUser);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && isAdminRole(currentUser);
    }

    @Transactional(readOnly = true)
    public boolean hasFeature(String featureName) {
        User currentUser = getCurrentUser();
        if (currentUser != null && isAdminRole(currentUser)) return true;
        GroupFeature feature = parseFeature(featureName);
        if (feature == null) return false;
        CollaborationGroup group = getDefaultGroup();
        if (currentUser == null || !group.isActive()) return false;
        if (hasRolePermission(group, currentUser, feature)) return true;
        return hasMemberPermission(group, currentUser, feature);
    }

    public GroupFeature[] getAllFeatures() {
        return GroupFeature.values();
    }

    public Set<String> getEnabledPermissionKeys(CollaborationGroup group) {
        return group.getRolePermissions().stream()
                .map(p -> permissionKey(p.getGroupRole(), p.getFeature()))
                .collect(Collectors.toSet());
    }

    public Set<String> getEnabledMemberPermissionKeys(CollaborationGroup group) {
        return group.getMemberPermissions().stream()
                .map(p -> permissionKey(p.getUser(), p.getFeature()))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPermissionSummary(User user) {
        CollaborationGroup group = getDefaultGroup();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (GroupFeature feature : GroupFeature.values()) {
            boolean roleGranted = isAdminRole(user) || hasRolePermission(group, user, feature);
            boolean userGranted = hasMemberPermission(group, user, feature);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("feature", feature);
            row.put("enabled", roleGranted || userGranted);
            String source = isAdminRole(user) ? "Admin Bypass"
                    : roleGranted ? "Role " + user.getEffectiveRoleName()
                    : userGranted ? "User override" : "Locked";
            row.put("source", source);
            rows.add(row);
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCurrentUserPermissionSummary() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return List.of();
        return getPermissionSummary(currentUser);
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private boolean isAdminRole(User user) {
        return "ADMIN".equals(user.getEffectiveRoleName());
    }

    private boolean isEffectiveMember(CollaborationGroup group, User user) {
        return isAdminRole(user)
                || hasAnyRolePermission(group, user)
                || isExplicitMember(group, user)
                || hasAnyMemberPermission(group, user);
    }

    private boolean isExplicitMember(CollaborationGroup group, User user) {
        return group.getMembers().stream()
                .anyMatch(m -> m.getId() != null && m.getId().equals(user.getId()));
    }

    private boolean hasAnyRolePermission(CollaborationGroup group, User user) {
        String roleName = user.getEffectiveRoleName();
        return group.getRolePermissions().stream()
                .anyMatch(p -> p.getGroupRole() != null && roleName.equals(p.getGroupRole().getName()));
    }

    private boolean hasRolePermission(CollaborationGroup group, User user, GroupFeature feature) {
        String roleName = user.getEffectiveRoleName();
        return group.getRolePermissions().stream()
                .anyMatch(p -> p.getGroupRole() != null
                        && roleName.equals(p.getGroupRole().getName())
                        && p.getFeature() == feature);
    }

    private boolean hasAnyMemberPermission(CollaborationGroup group, User user) {
        if (user == null || user.getId() == null) return false;
        return group.getMemberPermissions().stream()
                .anyMatch(p -> p.getUser() != null && user.getId().equals(p.getUser().getId()));
    }

    private boolean hasMemberPermission(CollaborationGroup group, User user, GroupFeature feature) {
        if (user == null || user.getId() == null || feature == null) return false;
        return group.getMemberPermissions().stream()
                .anyMatch(p -> p.getFeature() == feature
                        && p.getUser() != null && user.getId().equals(p.getUser().getId()));
    }

    /**
     * Parse role permission keys in format "roleId:FEATURE_NAME"
     */
    private Set<CollaborationGroupRolePermission> parseRolePermissions(CollaborationGroup group, Set<String> keys) {
        if (keys == null || keys.isEmpty()) return new HashSet<>();
        Set<CollaborationGroupRolePermission> permissions = new HashSet<>();
        for (String key : keys) {
            if (key == null) continue;
            String[] parts = key.split(":", 2);
            if (parts.length != 2) continue;
            try {
                Integer roleId = Integer.valueOf(parts[0]);
                GroupFeature feature = GroupFeature.valueOf(parts[1]);
                groupRoleRepository.findById(roleId).ifPresent(groupRole -> {
                    CollaborationGroupRolePermission p = new CollaborationGroupRolePermission();
                    p.setGroup(group);
                    p.setGroupRole(groupRole);
                    p.setFeature(feature);
                    permissions.add(p);
                });
            } catch (IllegalArgumentException ignored) {
                // skip malformed keys
            }
        }
        return permissions;
    }

    private Set<CollaborationGroupMemberPermission> parseMemberPermissions(CollaborationGroup group, Set<String> keys) {
        if (keys == null || keys.isEmpty()) return new HashSet<>();
        Set<CollaborationGroupMemberPermission> permissions = new HashSet<>();
        for (String key : keys) {
            if (key == null) continue;
            String[] parts = key.split(":", 2);
            if (parts.length != 2) continue;
            try {
                Integer userId = Integer.valueOf(parts[0]);
                GroupFeature feature = GroupFeature.valueOf(parts[1]);
                userRepository.findById(userId).ifPresent(user -> {
                    CollaborationGroupMemberPermission p = new CollaborationGroupMemberPermission();
                    p.setGroup(group);
                    p.setUser(user);
                    p.setFeature(feature);
                    permissions.add(p);
                });
            } catch (IllegalArgumentException ignored) {
                // skip malformed keys
            }
        }
        return permissions;
    }

    private CollaborationGroup createDefaultGroup() {
        CollaborationGroup group = new CollaborationGroup();
        group.setName(DEFAULT_GROUP_NAME);
        group.setDescription("Shared group with role-based feature permissions.");
        group.setCreatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    private void notifyNewAccess(Set<Integer> previousMemberIds,
                                 Set<String> previousMemberPermissionKeys,
                                 Set<User> members,
                                 Set<CollaborationGroupMemberPermission> memberPermissions) {
        members.stream()
                .filter(u -> u.getId() != null && !previousMemberIds.contains(u.getId()))
                .forEach(u -> notificationService.createNotification(
                        u,
                        "Bạn đã được thêm vào " + DEFAULT_GROUP_NAME + ".",
                        NotificationType.INFO,
                        "/user1/groups"));

        memberPermissions.stream()
                .filter(p -> !previousMemberPermissionKeys.contains(permissionKey(p.getUser(), p.getFeature())))
                .forEach(p -> notificationService.createNotification(
                        p.getUser(),
                        "Bạn được cấp quyền " + p.getFeature().getDisplayName() + " trong " + DEFAULT_GROUP_NAME + ".",
                        NotificationType.INFO,
                        "/user1/groups"));
    }

    private void auditGroupPermissionChange(CollaborationGroup group,
                                            Set<String> rolePermissionKeys,
                                            Set<String> memberPermissionKeys) {
        User actor = getCurrentUser();
        String actorName = actor != null && actor.getUsername() != null ? actor.getUsername() : "system";
        int roleCount = rolePermissionKeys != null ? rolePermissionKeys.size() : 0;
        int memberCount = memberPermissionKeys != null ? memberPermissionKeys.size() : 0;
        String detail = "Updated group[" + group.getId() + "] permissions: roles=" + roleCount
                + ", userOverrides=" + memberCount
                + ", effectiveMembers=" + getEffectiveMembersForGroup(group.getId()).size();
        hrAuditLogService.log(actorName, "GROUP_PERMISSION_UPDATE", "GROUP_ACCESS", String.valueOf(group.getId()), detail);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authUserHelper.getCurrentUser(authentication);
    }

    private static String permissionKey(GroupRole role, GroupFeature feature) {
        return (role != null ? role.getId() : "null") + ":" + feature.name();
    }

    private static String permissionKey(User user, GroupFeature feature) {
        return (user != null ? user.getId() : "null") + ":" + feature.name();
    }

    private GroupFeature parseFeature(String featureName) {
        if (featureName == null || featureName.isBlank()) return null;
        try {
            return GroupFeature.valueOf(featureName.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String displayName(User user) {
        if (user == null) return "";
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername() != null ? user.getUsername() : "";
    }
}
