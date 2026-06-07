package com.example.hr.models;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.OKRStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "okrs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OKR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "objective", nullable = false, columnDefinition = "TEXT")
    private String objective;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "okr_type", length = 50)
    private String okrType; // COMPANY, DEPARTMENT, TEAM, INDIVIDUAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_okr_id")
    private OKR parentOKR;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "progress")
    private Integer progress = 0; // 0-100

    @Column(name = "weight")
    private Integer weight = 100; // Trá»ng sá»‘

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private OKRStatus status = OKRStatus.NOT_STARTED;

    @OneToMany(mappedBy = "okr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyResult> keyResults = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}


