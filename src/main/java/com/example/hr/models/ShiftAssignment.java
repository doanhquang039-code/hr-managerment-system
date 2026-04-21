package com.example.hr.models;

import com.example.hr.enums.ShiftAssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "shift_assignment",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "work_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    private WorkShift shift;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "actual_check_in")
    private LocalTime actualCheckIn;

    @Column(name = "actual_check_out")
    private LocalTime actualCheckOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftAssignmentStatus status = ShiftAssignmentStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
