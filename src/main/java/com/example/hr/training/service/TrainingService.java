package com.example.hr.training.service;

import com.example.hr.training.dto.TrainingEnrollmentDTO;
import com.example.hr.training.dto.TrainingProgramDTO;
import com.example.hr.enums.EnrollmentStatus;
import com.example.hr.enums.TrainingStatus;
import com.example.hr.exception.BusinessValidationException;
import com.example.hr.exception.DuplicateResourceException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.department.entity.Department;
import com.example.hr.training.entity.TrainingEnrollment;
import com.example.hr.training.entity.TrainingProgram;
import com.example.hr.models.User;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.training.repository.TrainingEnrollmentRepository;
import com.example.hr.training.repository.TrainingProgramRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service quáº£n lÃ½ Ä‘Ã o táº¡o ná»™i bá»™.
 * Bao gá»“m: táº¡o chÆ°Æ¡ng trÃ¬nh, ghi danh, cháº¥m Ä‘iá»ƒm, cáº¥p chá»©ng chá»‰.
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
     * Láº¥y táº¥t cáº£ chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.
     */
    @Transactional(readOnly = true)
    public List<TrainingProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    /**
     * Láº¥y chÆ°Æ¡ng trÃ¬nh theo ID.
     */
    @Transactional(readOnly = true)
    public TrainingProgram getProgramById(Integer id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o", id));
    }

    /**
     * Láº¥y chÆ°Æ¡ng trÃ¬nh Ä‘ang hoáº¡t Ä‘á»™ng.
     */
    @Transactional(readOnly = true)
    public List<TrainingProgram> getActivePrograms() {
        return programRepository.findActivePrograms();
    }

    /**
     * Láº¥y chÆ°Æ¡ng trÃ¬nh sáº¯p tá»›i.
     */
    @Transactional(readOnly = true)
    public List<TrainingProgram> getUpcomingPrograms() {
        return programRepository.findUpcomingPrograms(LocalDate.now());
    }

    /**
     * Táº¡o chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o má»›i.
     */
    public TrainingProgram createProgram(TrainingProgramDTO dto) {
        // Validate dates
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessValidationException("NgÃ y káº¿t thÃºc pháº£i sau ngÃ y báº¯t Ä‘áº§u");
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
                    .orElseThrow(() -> new ResourceNotFoundException("PhÃ²ng ban", dto.getDepartmentId()));
            program.setDepartment(dept);
        }

        return programRepository.save(program);
    }

    /**
     * Cáº­p nháº­t chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.
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
                    .orElseThrow(() -> new ResourceNotFoundException("PhÃ²ng ban", dto.getDepartmentId()));
            program.setDepartment(dept);
        }

        return programRepository.save(program);
    }

    /**
     * Báº¯t Ä‘áº§u chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.
     */
    public TrainingProgram startProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        if (program.getStatus() != TrainingStatus.PLANNED) {
            throw new BusinessValidationException("Chá»‰ cÃ³ thá»ƒ báº¯t Ä‘áº§u chÆ°Æ¡ng trÃ¬nh á»Ÿ tráº¡ng thÃ¡i Káº¿ hoáº¡ch");
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
     * HoÃ n thÃ nh chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.
     */
    public TrainingProgram completeProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        if (program.getStatus() != TrainingStatus.IN_PROGRESS) {
            throw new BusinessValidationException("Chá»‰ cÃ³ thá»ƒ hoÃ n thÃ nh chÆ°Æ¡ng trÃ¬nh Ä‘ang diá»…n ra");
        }
        program.setStatus(TrainingStatus.COMPLETED);
        return programRepository.save(program);
    }

    /**
     * Há»§y chÆ°Æ¡ng trÃ¬nh Ä‘Ã o táº¡o.
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
     * XÃ³a chÆ°Æ¡ng trÃ¬nh (chá»‰ khi chÆ°a báº¯t Ä‘áº§u).
     */
    public void deleteProgram(Integer id) {
        TrainingProgram program = getProgramById(id);
        if (program.getStatus() == TrainingStatus.IN_PROGRESS) {
            throw new BusinessValidationException("KhÃ´ng thá»ƒ xÃ³a chÆ°Æ¡ng trÃ¬nh Ä‘ang diá»…n ra");
        }
        programRepository.delete(program);
    }

    // ===================== ENROLLMENTS =====================

    /**
     * Ghi danh nhÃ¢n viÃªn vÃ o chÆ°Æ¡ng trÃ¬nh.
     */
    public TrainingEnrollment enrollUser(Integer programId, Integer userId) {
        TrainingProgram program = getProgramById(programId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NhÃ¢n viÃªn", userId));

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndProgramId(userId, programId)) {
            throw new DuplicateResourceException("NhÃ¢n viÃªn Ä‘Ã£ ghi danh vÃ o chÆ°Æ¡ng trÃ¬nh nÃ y");
        }

        // Check capacity
        if (program.isFull()) {
            throw new BusinessValidationException("ChÆ°Æ¡ng trÃ¬nh Ä‘Ã£ Ä‘áº§y. Tá»‘i Ä‘a: " + program.getMaxCapacity());
        }

        // Check program status
        if (program.getStatus() == TrainingStatus.COMPLETED || program.getStatus() == TrainingStatus.CANCELLED) {
            throw new BusinessValidationException("KhÃ´ng thá»ƒ ghi danh vÃ o chÆ°Æ¡ng trÃ¬nh Ä‘Ã£ káº¿t thÃºc/há»§y");
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
     * Cháº¥m Ä‘iá»ƒm cho enrollment.
     */
    public TrainingEnrollment gradeEnrollment(Integer enrollmentId, BigDecimal score, String feedback) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessValidationException("Äiá»ƒm pháº£i tá»« 0 Ä‘áº¿n 100");
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
     * Bá» há»c.
     */
    public TrainingEnrollment dropEnrollment(Integer enrollmentId) {
        TrainingEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new BusinessValidationException("KhÃ´ng thá»ƒ bá» khÃ³a há»c Ä‘Ã£ hoÃ n thÃ nh");
        }

        enrollment.drop();
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Láº¥y enrollments cá»§a user.
     */
    @Transactional(readOnly = true)
    public List<TrainingEnrollment> getUserEnrollments(Integer userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    /**
     * Láº¥y enrollments Ä‘ang active cá»§a user.
     */
    @Transactional(readOnly = true)
    public List<TrainingEnrollment> getActiveEnrollments(Integer userId) {
        return enrollmentRepository.findActiveEnrollments(userId);
    }

    /**
     * Láº¥y enrollments cá»§a program.
     */
    @Transactional(readOnly = true)
    public List<TrainingEnrollment> getProgramEnrollments(Integer programId) {
        return enrollmentRepository.findByProgramId(programId);
    }

    /**
     * Tá»•ng ngÃ¢n sÃ¡ch Ä‘Ã o táº¡o theo nÄƒm.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBudgetByYear(int year) {
        return programRepository.sumBudgetByYear(year);
    }

    /**
     * Äiá»ƒm trung bÃ¬nh cá»§a chÆ°Æ¡ng trÃ¬nh.
     */
    @Transactional(readOnly = true)
    public Double getAverageScore(Integer programId) {
        return enrollmentRepository.getAverageScoreByProgram(programId);
    }

    /**
     * Sá»‘ lÆ°á»£ng hoÃ n thÃ nh cá»§a user.
     */
    @Transactional(readOnly = true)
    public long getCompletedCount(Integer userId) {
        return enrollmentRepository.countCompletedByUserId(userId);
    }
}

