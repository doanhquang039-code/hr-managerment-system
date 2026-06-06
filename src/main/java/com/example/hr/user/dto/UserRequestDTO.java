package com.example.hr.user.dto;

import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    private String username;

    @Size(max = 30, message = "Mã nhân viên tối đa 30 ký tự")
    private String employeeCode;

    @Size(min = 6, max = 255, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Pattern(
            regexp = "^(\\+?[0-9]{9,15})?$",
            message = "Số điện thoại chỉ gồm số, có thể bắt đầu bằng + và dài 9-15 ký tự")
    private String phoneNumber;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)?$", message = "Giới tính chỉ nhận MALE, FEMALE hoặc OTHER")
    private String gender;

    private LocalDate dateOfBirth;
    private LocalDate hireDate;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    private Integer departmentId;
    private Integer positionId;
    private Role role = Role.USER;
    private UserStatus status = UserStatus.ACTIVE;
}
