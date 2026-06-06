package com.example.hr.service;

import com.example.hr.dto.TrainingEnrollmentDTO;
import com.example.hr.dto.TrainingProgramDTO;
import com.example.hr.enums.EnrollmentStatus;
import com.example.hr.enums.TrainingStatus;
import com.example.hr.exception.BusinessValidationException;
import com.example.hr.exception.DuplicateResourceException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.models.Department;
import com.example.hr.models.TrainingEnrollment;
import com.example.hr.models.TrainingProgram;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.TrainingEnrollmentRepository;
import com.example.hr.repository.TrainingProgramRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quản lý đào tạo nội bộ.
 * Bao gồm: tạo chương trình, ghi danh, chấm điểm, cấp chứng chỉ.
 */
@Service
@Transactional
public class TrainingService {

    private final TrainingProgramRepository programRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public TrainingService(TrainingProgramRepository programRepository,
                            TrainingEnrollmentRepository enrollmentRepository,
                            UserRepository userRepository,
                            DepartmentRepository departmentRepository) {
        this.programRepository = programRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    // ===================== TRAINING PROGRAMS =====================

    /**
     * Lấy tất cả chương trình đào tạo.
     */
    @Transactional(readOnly = true)
    public List<TrainingProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    /**
     * Lấy chương trình theo ID.
     */
    @Transactional(readOnly = true)
    public TrainingProgram getProgramById(Integer id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chương trình đào tạo", id));
    }

    /**
     * Lấy chương trình đang hoạt động.
     */
    @Transactional(readOnly = true)
    public List<TrainingProgram> getActivePrograms() {
        return programRepository.findActivePrograms();
    }

    /**
     * Lấy chương trình sắp tới.
     */
    @Transactional(readOnly = true)
    public List<TrainingProgram> getUpcomingPrograms() {
        return programRepository.findUpcomingPrograms(LocalDate.now());
    }

    /**
     * Tạo chương trình đào tạo mới.
     */
    public TrainingProgram createProgram(TrainingProgramDTO dto) {
        // Validate dates
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessValidationException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        TrainingProgram program = new TrainingProgram();
        program.setProgramName(dto.getProgramName());
        program.setDescription(dto.getDescription());
        program.setInstructor(dto.getInstructor());
        program.setStartDate(dto.getStartDate());
        program.setEndDate(dto.getEndDate());
        program.setMaxCapacity(dto.getMaxCapacity() != null ? dto.getMaxCapacity() : 30);
        program.setLocation(dto.getLocation());
        program.setTrainingType(dto.getTrainingType() != null ? dto.getTrainingType() : "INTERNAL");
        program.setBudget(dto.getBudget() != null ? dto.getBudget() : BigDecimal.ZERO);
        program.setStatus(TrainingStatus.PLANNED);
        program.setCreatedAt(LocalDateTime.now());

        // Set department if provided
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", dto.getDepartmentId()));
            program.setDepartment(dept);
        }

        return programRepository.save(program);
    }

    /**
     * Cập nhật chương trình đào tạo.
     */
    public TrainingProgram updateProgram(Integer id, TrainingProgramDTO dto) {
        TrainingProgram program = getProgramById(id);

        if (dto.getProgramName() != null) program.setProgramName(dto.getProgramName());
        if (dto.getDescription() != null) program.setDescription(dto.getDescription());
        if (dto.getInstructor() != null) program.setInstructor(dto.getInstructor());
        if (dto.getStartDate() != null) program.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) program.setEndDate(dto.getEndDate());
        if (dto.getMaxCapacity() != null) program.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getLocation() != null) program.setLocation(dto.getLocation());
        if (dto.getTrainingType() != null) program.setTrainingType(dto.getTrainingType());
        if (dto.getBudget() != null) program.setBudget(dto.getBudget());

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", dto.getDepartmentId()));
            program.setDepartment(dept);
        }

        return programRepository.save(program);
    }

    /**
     * Bắt đầu chương trình đào tạo.
     */
    public TrainingProgram startProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        if (program.getStatus() != TrainingStatus.PLANNED) {
            throw new BusinessValidationException("Chỉ có thể bắt đầu chương trình ở trạng thái Kế hoạch");
        }
        program.setStatus(TrainingStatus.IN_PROGRESS);
        // Auto-update enrollments to IN_PROGRESS
        List<TrainingEnrollment> enrollments = enrollmentRepository.findByProgramId(id);
        List<TrainingEnrollment> enrollmentsToUpdate = new java.util.ArrayList<>();
        for (TrainingEnrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                enrollment.setStatus(EnrollmentStatus.IN_PROGRESS);
                enrollmentsToUpdate.add(enrollment);
            }
        }
        if (!enrollmentsToUpdate.isEmpty()) {
            enrollmentRepository.saveAll(enrollmentsToUpdate);
        }
        return programRepository.save(program);
    }

    /**
     * Hoàn thành chương trình đào tạo.
     */
    public TrainingProgram completeProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        if (program.getStatus() != TrainingStatus.IN_PROGRESS) {
            throw new BusinessValidationException("Chỉ có thể hoàn thành chương trình đang diễn ra");
        }
        program.setStatus(TrainingStatus.COMPLETED);
        return programRepository.save(program);
    }

    /**
     * Hủy chương trình đào tạo.
     */
    public TrainingProgram cancelProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        program.setStatus(TrainingStatus.CANCELLED);
        // Drop all enrollments
        List<TrainingEnrollment> enrollments = enrollmentRepository.findByProgramId(id);
        List<TrainingEnrollment> enrollmentsToUpdate = new java.util.ArrayList<>();
        for (TrainingEnrollment enrollment : enrollments) {
            if (enrollment.getStatus() != EnrollmentStatus.DROPPED) {
                enrollment.drop();
                enrollmentsToUpdate.add(enrollment);
            }
        }
        if (!enrollmentsToUpdate.isEmpty()) {
            enrollmentRepository.saveAll(enrollmentsToUpdate);
        }
        return programRepository.save(program);
    }

    /**
     * Xóa chương trình (chỉ khi chưa bắt đầu).
     */
    public void deleteProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        if (program.getStatus() == TrainingStatus.IN_PROGRESS) {
            throw new BusinessValidationException("Không thể xóa chương trình đang diễn ra");
        }
        programRepository.delete(program);
    }

    // ===================== ENROLLMENTS =====================

    /**
     * Ghi danh nhân viên vào chương trình.
     */
    public TrainingEnrollment enrollUser(Integer programId, Integer userId) {
        TrainingProgram program = getProgramById(programId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", userId));

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndProgramId(userId, programId)) {
            throw new DuplicateResourceException("Nhân viên đã ghi danh vào chương trình này");
        }

        // Check capacity
        if (program.isFull()) {
            throw new BusinessValidationException("Chương trình đã đầy. Tối đa: " + program.getMaxCapacity());
        }

        // Check program status
        if (program.getStatus() == TrainingStatus.COMPLETED || program.getStatus() == TrainingStatus.CANCELLED) {
            throw new BusinessValidationException("Không thể ghi danh vào chương trình đã kết thúc/hủy");
        }

        TrainingEnrollment enrollment = new TrainingEnrollment();
        enrollment.setUser(user);
        enrollment.setProgram(program);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(program.getStatus() == TrainingStatus.IN_PROGRESS
                ? EnrollmentStatus.IN_PROGRESS : EnrollmentStatus.ENROLLED);

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Chấm điểm cho enrollment.
     */
    public TrainingEnrollment gradeEnrollment(Integer enrollmentId, BigDecimal score, String feedback) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessValidationException("Điểm phải từ 0 đến 100");
        }

        enrollment.setFeedback(feedback);
        enrollment.complete(score);

        // Auto-generate certificate if passed
        if (enrollment.isPassed() && !enrollment.hasCertificate()) {
            String certUrl = "/api/certificates/download/" + enrollmentId + "-" + System.currentTimeMillis() + ".pdf";
            enrollment.setCertificateUrl(certUrl);
        }

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Bỏ học.
     */
    public TrainingEnrollment dropEnrollment(Integer enrollmentId) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new BusinessValidationException("Không thể bỏ khóa học đã hoàn thành");
        }

        enrollment.drop();
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Lấy enrollments của user.
     */
    @Transactional(readOnly = true)
    public List<TrainingEnrollment> getUserEnrollments(Integer userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    /**
     * Lấy enrollments đang active của user.
     */
    @Transactional(readOnly = true)
    public List<TrainingEnrollment> getActiveEnrollments(Integer userId) {
        return enrollmentRepository.findActiveEnrollments(userId);
    }

    /**
     * Lấy enrollments của program.
     */
    @Transactional(readOnly = true)
    public List<TrainingEnrollment> getProgramEnrollments(Integer programId) {
        return enrollmentRepository.findByProgramId(programId);
    }

    /**
     * Tổng ngân sách đào tạo theo năm.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBudgetByYear(int year) {
        return programRepository.sumBudgetByYear(year);
    }

    /**
     * Điểm trung bình của chương trình.
     */
    @Transactional(readOnly = true)
    public Double getAverageScore(Integer programId) {
        return enrollmentRepository.getAverageScoreByProgram(programId);
    }

    /**
     * Số lượng hoàn thành của user.
     */
    @Transactional(readOnly = true)
    public long getCompletedCount(Integer userId) {
        return enrollmentRepository.countCompletedByUserId(userId);
    }
}
