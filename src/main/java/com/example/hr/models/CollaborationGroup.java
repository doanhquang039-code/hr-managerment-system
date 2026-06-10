package com.example.hr.models;

import com.example.hr.enums.GroupFeature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * CollaborationGroup — supports multiple independent groups.
 * Each group has its own role-feature permissions and explicit member list.
 * Admins can CRUD groups from /admin/groups UI.
 */
@Entity
@Table(name = "collaboration_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_class", length = 50)
    private String iconClass = "bi-diagram-3";

    @Column(nullable = false)
    private boolean active = true;

    /** Explicit members added by admin regardless of role */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "collaboration_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    /**
     * Role-level feature permissions: which GroupRole can access which GroupFeature.
     * When a user's groupRole matches a permission here, they get that feature.
     */
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CollaborationGroupRolePermission> rolePermissions = new HashSet<>();

    /**
     * Per-user feature overrides: individual users granted specific features
     * regardless of their role (e.g., a USER role user who gets TASKS access).
     */
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CollaborationGroupMemberPermission> memberPermissions = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Convenience: compute which GroupRoles are represented in rolePermissions */
    public Set<GroupRole> getConfiguredRoles() {
        Set<GroupRole> roles = new HashSet<>();
        for (CollaborationGroupRolePermission p : rolePermissions) {
            if (p.getGroupRole() != null) roles.add(p.getGroupRole());
        }
        return roles;
    }

    /** Convenience: compute which features are enabled for a given GroupRole */
    public Set<GroupFeature> getFeaturesForRole(GroupRole role) {
        Set<GroupFeature> features = new HashSet<>();
        for (CollaborationGroupRolePermission p : rolePermissions) {
            if (p.getGroupRole() != null && p.getGroupRole().getId().equals(role.getId())) {
                features.add(p.getFeature());
            }
        }
        return features;
    }
}
