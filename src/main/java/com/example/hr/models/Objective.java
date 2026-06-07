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
@Table(name = "objectives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Objective {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OKRStatus status = OKRStatus.DRAFT;

    @Column(nullable = false)
    private String level; // COMPANY, DEPARTMENT, INDIVIDUAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_objective_id")
    private Objective parentObjective;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double progress = 0.0;

    @OneToMany(mappedBy = "objective", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyResult> keyResults = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateProgress() {
        if (keyResults == null || keyResults.isEmpty()) {
            this.progress = 0.0;
            return;
        }
        double totalProgress = keyResults.stream()
                .mapToDouble(KeyResult::getProgress)
                .average()
                .orElse(0.0);
        this.progress = Math.round(totalProgress * 100.0) / 100.0;
    }
}


