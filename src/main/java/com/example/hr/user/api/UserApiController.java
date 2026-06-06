package com.example.hr.user.api;

import com.example.hr.user.dto.UserRequestDTO;
import com.example.hr.user.dto.UserResponseDTO;
import com.example.hr.enums.UserStatus;
import com.example.hr.exception.BusinessValidationException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.Department;
import com.example.hr.models.JobPosition;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserApiController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserApiController(UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             JobPositionRepository positionRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ACTIVE") UserStatus status) {
        List<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository
                    .findByStatusAndFullNameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCaseOrStatusAndEmployeeCodeContainingIgnoreCase(
                            status, keyword, status, keyword, status, keyword);
        } else {
            users = userRepository.findByStatus(status);
        }
        return ResponseEntity.ok(users.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> get(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessValidationException("Mật khẩu là bắt buộc khi tạo mới");
        }
        validateUnique(dto, null);
        User user = mapToUser(dto, new User(), true);
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody UserRequestDTO dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
        validateUnique(dto, id);
        User updated = mapToUser(dto, existing, false);
        User saved = userRepository.save(updated);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDTO> deactivate(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
        user.setStatus(UserStatus.INACTIVE);
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    private void validateUnique(UserRequestDTO dto, Integer currentId) {
        if (currentId == null) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new BusinessValidationException("Tên đăng nhập đã tồn tại");
            }
            if (dto.getEmail() != null && !dto.getEmail().isBlank() && userRepository.existsByEmail(dto.getEmail())) {
                throw new BusinessValidationException("Email đã tồn tại");
            }
            if (dto.getEmployeeCode() != null && !dto.getEmployeeCode().isBlank()
                    && userRepository.existsByEmployeeCode(dto.getEmployeeCode())) {
                throw new BusinessValidationException("Mã nhân viên đã tồn tại");
            }
            return;
        }

        if (userRepository.existsByUsernameAndIdNot(dto.getUsername(), currentId)) {
            throw new BusinessValidationException("Tên đăng nhập đã tồn tại");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && userRepository.existsByEmailAndIdNot(dto.getEmail(), currentId)) {
            throw new BusinessValidationException("Email đã tồn tại");
        }
        if (dto.getEmployeeCode() != null && !dto.getEmployeeCode().isBlank()
                && userRepository.existsByEmployeeCodeAndIdNot(dto.getEmployeeCode(), currentId)) {
            throw new BusinessValidationException("Mã nhân viên đã tồn tại");
        }
    }

    private User mapToUser(UserRequestDTO dto, User target, boolean isCreate) {
        target.setUsername(dto.getUsername());
        target.setEmployeeCode(dto.getEmployeeCode());
        target.setFullName(dto.getFullName());
        target.setEmail(dto.getEmail());
        target.setPhoneNumber(dto.getPhoneNumber());
        target.setGender(dto.getGender());
        target.setDateOfBirth(dto.getDateOfBirth());
        target.setHireDate(dto.getHireDate());
        target.setAddress(dto.getAddress());
        target.setRole(dto.getRole());
        target.setStatus(dto.getStatus());

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban id=" + dto.getDepartmentId()));
            target.setDepartment(department);
        } else {
            target.setDepartment(null);
        }

        if (dto.getPositionId() != null) {
            JobPosition position = positionRepository.findById(dto.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức vụ id=" + dto.getPositionId()));
            target.setPosition(position);
        } else {
            target.setPosition(null);
        }

        if (isCreate || (dto.getPassword() != null && !dto.getPassword().isBlank())) {
            target.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return target;
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmployeeCode(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getHireDate(),
                user.getAddress(),
                user.getProfileImage(),
                user.getRole(),
                user.getStatus(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null,
                user.getPosition() != null ? user.getPosition().getId() : null,
                user.getPosition() != null ? user.getPosition().getPositionName() : null,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
