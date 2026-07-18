package com.example.hr.graphql.controller;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.User;
import com.example.hr.department.entity.Department;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.recruitment.entity.JobPosition;
import com.example.hr.recruitment.repository.JobPositionRepository;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.user.service.UserService;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HrGraphQLController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JobPositionRepository positionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Queries ---

    @QueryMapping
    public User employee(@Argument String id) {
        return userRepository.findById(Integer.valueOf(id)).orElse(null);
    }

    @QueryMapping
    public Map<String, Object> employees(@Argument Integer page, @Argument Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        Pageable pageable = PageRequest.of(p, s);
        Page<User> userPage = userRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("content", userPage.getContent());
        result.put("totalElements", (int) userPage.getTotalElements());
        result.put("totalPages", userPage.getTotalPages());
        result.put("number", userPage.getNumber());
        result.put("size", userPage.getSize());
        return result;
    }

    @QueryMapping
    public List<User> searchEmployees(@Argument String keyword) {
        return userRepository.findByFullNameContainingAndStatus(keyword, UserStatus.ACTIVE);
    }

    @QueryMapping
    public Department department(@Argument String id) {
        return departmentRepository.findById(Integer.valueOf(id)).orElse(null);
    }

    @QueryMapping
    public List<Department> departments() {
        return departmentRepository.findAll();
    }

    @QueryMapping
    public Attendance attendance(@Argument String id) {
        return attendanceRepository.findById(Integer.valueOf(id)).orElse(null);
    }

    @QueryMapping
    public List<Attendance> attendanceByUser(
            @Argument String userId,
            @Argument String startDate,
            @Argument String endDate) {
        User user = userRepository.findById(Integer.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return attendanceRepository.findByUserAndAttendanceDateBetweenOrderByAttendanceDateDesc(user, start, end);
    }

    // --- Schema Mappings ---

    @SchemaMapping(typeName = "Employee", field = "phone")
    public String getPhone(User user) {
        return user.getPhoneNumber();
    }

    @SchemaMapping(typeName = "Employee", field = "position")
    public String getPosition(User user) {
        return user.getPosition() != null ? user.getPosition().getPositionName() : null;
    }

    @SchemaMapping(typeName = "Employee", field = "hireDate")
    public String getHireDate(User user) {
        return user.getHireDate() != null ? user.getHireDate().toString() : null;
    }

    @SchemaMapping(typeName = "Employee", field = "status")
    public String getStatus(User user) {
        return user.getStatus() != null ? user.getStatus().name() : null;
    }

    @SchemaMapping(typeName = "Department", field = "name")
    public String getName(Department department) {
        return department.getDepartmentName();
    }

    @SchemaMapping(typeName = "Department", field = "description")
    public String getDescription(Department department) {
        return department.getWorkingHours(); // Using workingHours as fallback for description since description doesn't exist
    }

    @SchemaMapping(typeName = "Department", field = "employees")
    public List<User> getEmployees(Department department) {
        return userRepository.findByDepartment(department);
    }

    @SchemaMapping(typeName = "Attendance", field = "checkInTime")
    public String getCheckInTime(Attendance attendance) {
        return attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : null;
    }

    @SchemaMapping(typeName = "Attendance", field = "checkOutTime")
    public String getCheckOutTime(Attendance attendance) {
        return attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : null;
    }

    @SchemaMapping(typeName = "Attendance", field = "workingHours")
    public Double getWorkingHours(Attendance attendance) {
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            return java.time.Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes() / 60.0;
        }
        return 0.0;
    }

    @SchemaMapping(typeName = "Attendance", field = "status")
    public String getAttendanceStatus(Attendance attendance) {
        return attendance.getStatus() != null ? attendance.getStatus().name() : null;
    }

    // --- Mutations ---

    @MutationMapping
    public User createEmployee(@Argument Map<String, Object> input) {
        User user = new User();
        user.setUsername((String) input.get("username"));
        user.setFullName((String) input.get("fullName"));
        user.setEmail((String) input.get("email"));
        user.setPhoneNumber((String) input.get("phone"));
        user.setPassword(passwordEncoder.encode("123456")); // default password
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(com.example.hr.enums.Role.USER);

        String hireDateStr = (String) input.get("hireDate");
        if (hireDateStr != null) {
            user.setHireDate(LocalDate.parse(hireDateStr));
        } else {
            user.setHireDate(LocalDate.now());
        }

        String departmentIdStr = (String) input.get("departmentId");
        if (departmentIdStr != null) {
            departmentRepository.findById(Integer.valueOf(departmentIdStr)).ifPresent(user::setDepartment);
        }

        String positionName = (String) input.get("position");
        if (positionName != null) {
            List<JobPosition> positions = positionRepository.findByPositionNameContainingIgnoreCase(positionName);
            if (!positions.isEmpty()) {
                user.setPosition(positions.get(0));
            }
        }

        user.setEmployeeCode(generateDefaultEmployeeCode());
        userService.registerNewUser(user);
        return user;
    }

    @MutationMapping
    public User updateEmployee(@Argument String id, @Argument Map<String, Object> input) {
        User user = userRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (input.containsKey("username")) {
            user.setUsername((String) input.get("username"));
        }
        if (input.containsKey("fullName")) {
            user.setFullName((String) input.get("fullName"));
        }
        if (input.containsKey("email")) {
            user.setEmail((String) input.get("email"));
        }
        if (input.containsKey("phone")) {
            user.setPhoneNumber((String) input.get("phone"));
        }
        if (input.containsKey("hireDate")) {
            String hireDateStr = (String) input.get("hireDate");
            user.setHireDate(hireDateStr != null ? LocalDate.parse(hireDateStr) : null);
        }
        if (input.containsKey("departmentId")) {
            String departmentIdStr = (String) input.get("departmentId");
            if (departmentIdStr != null) {
                departmentRepository.findById(Integer.valueOf(departmentIdStr)).ifPresent(user::setDepartment);
            } else {
                user.setDepartment(null);
            }
        }
        if (input.containsKey("position")) {
            String positionName = (String) input.get("position");
            if (positionName != null) {
                List<JobPosition> positions = positionRepository.findByPositionNameContainingIgnoreCase(positionName);
                if (!positions.isEmpty()) {
                    user.setPosition(positions.get(0));
                }
            } else {
                user.setPosition(null);
            }
        }

        return userRepository.save(user);
    }

    @MutationMapping
    public Boolean deleteEmployee(@Argument String id) {
        try {
            userService.softDeleteUser(Integer.valueOf(id));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @MutationMapping
    public Attendance checkIn(@Argument String userId, @Argument Double latitude, @Argument Double longitude) {
        User user = userRepository.findById(Integer.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByUserAndAttendanceDate(user, today);
        if (existing.isEmpty()) {
            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setAttendanceDate(today);
            LocalTime now = LocalTime.now();
            attendance.setCheckInTime(now);

            LocalTime lateThreshold = LocalTime.of(8, 30);
            attendance.setStatus(now.isAfter(lateThreshold)
                    ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

            return attendanceRepository.save(attendance);
        }
        return existing.get();
    }

    @MutationMapping
    public Attendance checkOut(@Argument String userId) {
        User user = userRepository.findById(Integer.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Optional<Attendance> existing = attendanceRepository.findByUserAndAttendanceDate(user, LocalDate.now());
        if (existing.isPresent()) {
            Attendance attendance = existing.get();
            LocalTime now = LocalTime.now();
            attendance.setCheckOutTime(now);

            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                LocalTime earlyThreshold = LocalTime.of(17, 30);
                if (now.isBefore(earlyThreshold)) {
                    attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
                }
            }
            return attendanceRepository.save(attendance);
        }
        throw new IllegalStateException("No check-in record found for today");
    }

    private String generateDefaultEmployeeCode() {
        List<User> allUsers = userRepository.findAll();
        int maxNumber = 0;

        for (User u : allUsers) {
            String code = u.getEmployeeCode();
            if (code != null && code.startsWith("NV")) {
                try {
                    int num = Integer.parseInt(code.substring(2));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int nextNumber = maxNumber + 1;
        String newCode = String.format("NV%05d", nextNumber);

        while (userRepository.findByEmployeeCode(newCode).isPresent()) {
            nextNumber++;
            newCode = String.format("NV%05d", nextNumber);
        }

        return newCode;
    }
}
