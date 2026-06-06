package com.example.hr.user.dto;

import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private String address;
    private String profileImage;
    private Role role;
    private UserStatus status;
    private Integer departmentId;
    private String departmentName;
    private Integer positionId;
    private String positionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
