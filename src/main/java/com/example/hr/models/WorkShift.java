package com.example.hr.models;

import com.example.hr.enums.ShiftType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "work_shift")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shift_name", nullable = false, length = 100)
    private String shiftName;

    @Column(name = "shift_code", unique = true, nullable = false, length = 20)
    private String shiftCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType = ShiftType.REGULAR;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_minutes")
    private Integer breakMinutes = 60;

    @Column(name = "working_hours", insertable = false, updatable = false, precision = 4, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "allowance", precision = 15, scale = 2)
    private BigDecimal allowance = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
