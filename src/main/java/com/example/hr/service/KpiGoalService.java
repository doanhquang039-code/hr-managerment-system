package com.example.hr.service;

import com.example.hr.enums.KpiStatus;
import com.example.hr.models.KpiGoal;
import com.example.hr.models.User;
import com.example.hr.repository.KpiGoalRepository;
import com.example.hr.repository.UserRepository;
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

    public KpiGoalService(KpiGoalRepository kpiGoalRepository, UserRepository userRepository) {
        this.kpiGoalRepository = kpiGoalRepository;
        this.userRepository = userRepository;
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
        if (goal.getId() == null) {
            goal.setCreatedAt(LocalDateTime.now());
        }
        goal.setUpdatedAt(LocalDateTime.now());
        return kpiGoalRepository.save(goal);
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
