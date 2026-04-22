package com.example.hr.service;

import com.example.hr.enums.KpiStatus;
import com.example.hr.models.KpiGoal;
import com.example.hr.models.User;
import com.example.hr.repository.KpiGoalRepository;
import com.example.hr.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class KpiGoalService {

    private final KpiGoalRepository kpiGoalRepository;
    private final UserRepository userRepository;
    private final EmailFacade emailFacade;

    public KpiGoalService(KpiGoalRepository kpiGoalRepository,
                           UserRepository userRepository,
                           @Lazy EmailFacade emailFacade) {
        this.kpiGoalRepository = kpiGoalRepository;
        this.userRepository = userRepository;
        this.emailFacade = emailFacade;
    }

    public List<KpiGoal> findAll() {
        return kpiGoalRepository.findAll();
    }

    public List<KpiGoal> findByUser(Integer userId) {
        return kpiGoalRepository.findByUserId(userId);
    }

    public List<KpiGoal> findActiveByUser(Integer userId) {
        return kpiGoalRepository.findActiveGoalsByUser(userId, LocalDate.now());
    }

    public List<KpiGoal> findByStatus(KpiStatus status) {
        return kpiGoalRepository.findByStatus(status);
    }

    public Optional<KpiGoal> findById(Integer id) {
        return kpiGoalRepository.findById(id);
    }

    public KpiGoal save(KpiGoal goal) {
        boolean isNew = goal.getId() == null;
        if (isNew) {
            goal.setCreatedAt(LocalDateTime.now());
        }
        goal.setUpdatedAt(LocalDateTime.now());
        KpiGoal saved = kpiGoalRepository.save(goal);

        // Gửi email thông báo khi tạo KPI mới
        if (isNew && saved.getUser() != null) {
            User u = saved.getUser();
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                emailFacade.sendKpiAssigned(u.getEmail(), u.getFullName(),
                        saved.getGoalTitle(),
                        saved.getEndDate() != null ? saved.getEndDate().toString() : "N/A");
            }
        }
        return saved;
    }

    public void delete(Integer id) {
        kpiGoalRepository.deleteById(id);
    }

    /** Cập nhật tiến độ KPI */
    public KpiGoal updateProgress(Integer id, BigDecimal currentValue) {
        KpiGoal goal = kpiGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KPI Goal không tồn tại: " + id));
        goal.setCurrentValue(currentValue);
        goal.setUpdatedAt(LocalDateTime.now());

        // Tự động đánh dấu COMPLETED nếu đạt 100%
        if (goal.getTargetValue() != null && goal.getTargetValue().compareTo(BigDecimal.ZERO) > 0) {
            if (currentValue.compareTo(goal.getTargetValue()) >= 0) {
                goal.setStatus(KpiStatus.COMPLETED);
            }
        }
        return kpiGoalRepository.save(goal);
    }

    public Double getAvgAchievement(Integer userId) {
        Double avg = kpiGoalRepository.avgAchievementByUser(userId);
        return avg != null ? avg : 0.0;
    }

    public long countByStatus(KpiStatus status) {
        return kpiGoalRepository.findByStatus(status).size();
    }
}
