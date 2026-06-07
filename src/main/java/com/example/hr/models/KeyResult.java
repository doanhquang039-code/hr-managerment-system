package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "key_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "okr_id")
    private OKR okr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objective_id")
    private Objective objective;

    @Column(name = "key_result", nullable = false, columnDefinition = "TEXT")
    private String keyResult;

    @Column(name = "metric_type", length = 50)
    private String metricType; // NUMBER, PERCENTAGE, BOOLEAN, CURRENCY

    @Column(name = "start_value", precision = 15, scale = 2)
    private BigDecimal startValue;

    @Column(name = "target_value", precision = 15, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "unit", length = 50)
    private String unit; // %, VND, hours, etc.

    @Column(name = "progress")
    private Integer progress = 0; // 0-100

    @Column(name = "weight")
    private Integer weight = 100;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Alias methods for compatibility
    public Objective getObjective() {
        return this.objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    public void setTitle(String title) {
        this.keyResult = title;
    }

    public void setDescription(String description) {
        this.keyResult = description;
    }

    public void setMeasurementType(String measurementType) {
        this.metricType = measurementType;
    }

    // Calculate progress based on current vs target value
    public void calculateProgress() {
        if (startValue != null && targetValue != null && currentValue != null) {
            BigDecimal range = targetValue.subtract(startValue);
            if (range.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal achieved = currentValue.subtract(startValue);
                BigDecimal progressDecimal = achieved.divide(range, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                
                this.progress = Math.max(0, Math.min(100, progressDecimal.intValue()));
                
                if (this.progress >= 100) {
                    this.isCompleted = true;
                    this.completedAt = LocalDateTime.now();
                }
            }
        }
    }
}


