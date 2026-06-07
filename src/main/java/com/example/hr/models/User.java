package com.example.hr.models;


import com.example.hr.department.entity.Department;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.recruitment.entity.JobPosition;
import com.example.hr.security.SensitiveStringCryptoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor  // Cáº§n thiáº¿t cho JPA
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "employee_code", unique = true, length = 30)
    private String employeeCode;

@Column(nullable = false, length = 255)
private String password;
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Convert(converter = SensitiveStringCryptoConverter.class)
    @Column(name = "cccd", length = 1000)
    private String cccd;

    @Column(length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 255)
    private String address;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "profile_image")
    private String profileImage = "default_avatar.png";

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id")
    private JobPosition position;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by_encrypted", length = 1000)
    private String createdByEncrypted;

    @Column(name = "updated_by_encrypted", length = 1000)
    private String updatedByEncrypted;

    /** Firebase Cloud Messaging token â€” dÃ¹ng cho push notifications */
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    public String getMaskedCccd() {
        if (cccd == null || cccd.isBlank()) {
            return "ChÆ°a cÃ³ CCCD";
        }
        if (cccd.length() <= 6) {
            return "******";
        }
        return cccd.substring(0, 3) + "******" + cccd.substring(cccd.length() - 3);
    }
}


