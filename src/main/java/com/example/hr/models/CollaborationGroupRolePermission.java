package com.example.hr.models;

import com.example.hr.enums.GroupFeature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps a GroupRole to a GroupFeature within a CollaborationGroup.
 * Replaces the old enum-based Role with dynamic GroupRole entity.
 */
@Entity
@Table(
        name = "collaboration_group_role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "group_role_id", "feature"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationGroupRolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CollaborationGroup group;

    /** Dynamic role from DB */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_role_id", nullable = false)
    private GroupRole groupRole;

    @Column(nullable = false, length = 50)
    @jakarta.persistence.Convert(converter = com.example.hr.config.GroupFeatureConverter.class)
    private GroupFeature feature;
}
