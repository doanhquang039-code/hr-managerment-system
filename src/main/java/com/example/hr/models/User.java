package com.example.hr.models;

import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor  // Cần thiết cho JPA
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
}