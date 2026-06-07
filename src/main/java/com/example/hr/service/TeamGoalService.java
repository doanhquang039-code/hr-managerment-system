package com.example.hr.service;

import com.example.hr.models.TeamGoal;
import com.example.hr.department.entity.Department;
import com.example.hr.models.User;
import com.example.hr.repository.TeamGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamGoalService {

    private final TeamGoalRepository teamGoalRepository;

    /**
     * Create new team goal
     */
    @Transactional
    public TeamGoal createGoal(TeamGoal goal) {
        goal.setCreatedAt(LocalDateTime.now());
        goal.setStatus("NOT_STARTED");
        goal.setProgressPercentage(0);
        goal.setCurrentValue(0);
        return teamGoalRepository.save(goal);
    }

    /**
     * Update goal progress
     */
    @Transactional
    public TeamGoal updateProgress(Integer id, Integer currentValue) {
        TeamGoal goal = getGoalById(id);
        goal.setCurrentValue(currentValue);
        goal.calculateProgress();
        goal.setUpdatedAt(LocalDateTime.now());
        
        if (goal.getStatus().equals("NOT_STARTED")) {
            goal.setStatus("IN_PROGRESS");
        }
        
        return teamGoalRepository.save(goal);
    }

    /**
     * Update goal
     */
    @Transactional
    public TeamGoal updateGoal(Integer id, TeamGoal goalData) {
        TeamGoal goal = getGoalById(id);
        
        goal.setTitle(goalData.getTitle());
        goal.setDescription(goalData.getDescription());
        goal.setTargetValue(goalData.getTargetValue());
        goal.setEndDate(goalData.getEndDate());
        goal.setPriority(goalData.getPriority());
        goal.setNotes(goalData.getNotes());
        goal.setUpdatedAt(LocalDateTime.now());
        
        goal.calculateProgress();
        
        return teamGoalRepository.save(goal);
    }

    /**
     * Complete goal
     */
    @Transactional
    public TeamGoal completeGoal(Integer id) {
        TeamGoal goal = getGoalById(id);
        goal.setStatus("COMPLETED");
        goal.setProgressPercentage(100);
        goal.setUpdatedAt(LocalDateTime.now());
        return teamGoalRepository.save(goal);
    }

    /**
     * Cancel goal
     */
    @Transactional
    public void cancelGoal(Integer id) {
        TeamGoal goal = getGoalById(id);
        goal.setStatus("CANCELLED");
        goal.setUpdatedAt(LocalDateTime.now());
        teamGoalRepository.save(goal);
    }

    /**
     * Get goal by ID
     */
    public TeamGoal getGoalById(Integer id) {
        return teamGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team goal not found with id: " + id));
    }

    /**
     * Get goals by department
     */
    public List<TeamGoal> getGoalsByDepartment(Department department) {
        return teamGoalRepository.findByDepartmentOrderByCreatedAtDesc(department);
    }

    /**
     * Get active goals by manager
     */
    public List<TeamGoal> getActiveGoalsByManager(User manager) {
        return teamGoalRepository.findActiveGoalsByManager(manager, LocalDate.now());
    }

    /**
     * Get goals by status
     */
    public List<TeamGoal> getGoalsByStatus(String status) {
        return teamGoalRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get all goals
     */
    public List<TeamGoal> getAllGoals() {
        return teamGoalRepository.findAll();
    }

    /**
     * Get goal statistics
     */
    public GoalStatistics getGoalStatistics(Department department) {
        List<TeamGoal> allGoals = teamGoalRepository.findByDepartmentOrderByCreatedAtDesc(department);
        
        long totalGoals = allGoals.size();
        long completedGoals = allGoals.stream().filter(g -> "COMPLETED".equals(g.getStatus())).count();
        long inProgressGoals = allGoals.stream().filter(g -> "IN_PROGRESS".equals(g.getStatus())).count();
        long notStartedGoals = allGoals.stream().filter(g -> "NOT_STARTED".equals(g.getStatus())).count();
        
        Double avgProgress = teamGoalRepository.getAverageProgressByDepartment(department);
        
        return new GoalStatistics(
                totalGoals,
                completedGoals,
                inProgressGoals,
                notStartedGoals,
                avgProgress != null ? avgProgress : 0.0
        );
    }

    public record GoalStatistics(
            long totalGoals,
            long completedGoals,
            long inProgressGoals,
            long notStartedGoals,
            double avgProgress
    ) {}
}


