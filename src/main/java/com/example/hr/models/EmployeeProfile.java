package com.example.hr.models;

import com.example.hr.security.SensitiveStringCryptoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    
    private String bankName;

    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(length = 1000)
    private String bankAccountNumber;

    private String bankAccountName;

    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(name = "identity_number", length = 1000)
    private String identityNumber;
    
    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(length = 1000)
    private String taxCode;

    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(length = 1000)
    private String socialInsuranceNumber;

    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(length = 1000)
    private String healthInsuranceNumber;
    
    private String permanentAddress;
    private String currentAddress;
    
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String nationality;
    private String ethnicity;
    private String religion;
    
    private String maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(columnDefinition = "TEXT")
    private String skills;
    
    @Column(columnDefinition = "TEXT")
    private String certifications;
    
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
}


