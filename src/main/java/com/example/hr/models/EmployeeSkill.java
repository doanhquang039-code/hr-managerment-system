package com.example.hr.models;

import com.example.hr.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_skill",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "skill_category", length = 50)
    private String skillCategory = "TECHNICAL"; // TECHNICAL, SOFT, LANGUAGE, MANAGEMENT

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false)
    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    @Column(name = "years_exp", precision = 4, scale = 1)
    private BigDecimal yearsExp = BigDecimal.ZERO;

    @Column(name = "is_certified")
    private Boolean isCertified = false;

    @Column(name = "certificate_name", length = 200)
    private String certificateName;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
