package com.example.hr.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * GroupRole - Dynamic role entity replacing the hardcoded Role enum.
 * Stored in DB so admins can add/edit/delete roles from the UI.
 * The `name` field is used as Spring Security authority: "ROLE_{name}"
 */
@Entity
@Table(name = "group_roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Technical name used in Spring Security, e.g. ADMIN, MANAGER */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Human-readable label shown in UI, e.g. "Quản trị viên" */
    @Column(nullable = false, length = 100)
    private String displayName;

    /** Hex color for the badge in UI, e.g. "#e0f2fe" */
    @Column(length = 20)
    private String color;

    /** Badge text color, e.g. "#075985" */
    @Column(length = 20)
    private String textColor;

    /** Order for display in UI */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** True = built-in role that cannot be deleted */
    @Column(nullable = false)
    @Builder.Default
    private boolean builtIn = false;
}
