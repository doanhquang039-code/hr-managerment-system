package com.example.hr.service;



import com.example.hr.department.entity.Department;
import com.example.hr.payroll.entity.Payroll;
import com.example.hr.dto.AiInsightResponse;
import com.example.hr.dto.AiAdvisorResponse;
import com.example.hr.enums.KpiStatus;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.TaskStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.KpiGoal;
import com.example.hr.task.entity.TaskAssignment;
import com.example.hr.models.User;
import com.example.hr.repository.KpiGoalRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.task.repository.TaskAssignmentRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AiHrInsightService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private KpiGoalRepository kpiGoalRepository;

    @Autowired(required = false)
    private GeminiAiService geminiAiService;

    @Transactional(readOnly = true)
    public AiInsightResponse generate(User actor) {
        String scope = resolveScope(actor);
        Map<String, Object> metrics = buildMetrics(actor);
        List<String> recommendations = buildLocalRecommendations(metrics);
        String localSummary = buildLocalSummary(scope, metrics, recommendations);

        String mode = "rule-based";
        String summary = localSummary;
        if (geminiAiService != null && geminiAiService.isAvailable()) {
            String aiSummary = geminiAiService.chat(buildSystemPrompt(), buildUserPrompt(scope, metrics));
            if (aiSummary != null && !aiSummary.isBlank()) {
                mode = "gemini";
                summary = aiSummary.trim();
            }
        }

        return AiInsightResponse.builder()
                .scope(scope)
                .mode(mode)
                .summary(summary)
                .metrics(metrics)
                .recommendations(recommendations)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public AiAdvisorResponse advise(User actor, String rawQuestion) {
        String question = rawQuestion != null ? rawQuestion.trim() : "";
        String scope = resolveScope(actor);
        Map<String, Object> metrics = buildMetrics(actor);
        List<String> recommendations = buildLocalRecommendations(metrics);

        String mode = "rule-based";
        String answer = buildLocalAdvisorAnswer(scope, metrics, recommendations, question);

        if (!question.isBlank() && geminiAiService != null && geminiAiService.isAvailable()) {
            String aiAnswer = geminiAiService.chat(buildAdvisorSystemPrompt(), buildAdvisorUserPrompt(scope, metrics, recommendations, question));
            if (aiAnswer != null && !aiAnswer.isBlank()) {
                mode = "gemini";
                answer = aiAnswer.trim();
            }
        }

        return AiAdvisorResponse.builder()
                .scope(scope)
                .mode(mode)
                .answer(answer)
                .recommendations(recommendations)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private String resolveScope(User actor) {
        if (actor != null && actor.getRole() != null && "MANAGER".equals(actor.getRole().name())) {
            String department = actor.getDepartment() != null ? actor.getDepartment().getDepartmentName() : "unassigned department";
            return "department:" + department;
        }
        return "organization";
    }

    private Map<String, Object> buildMetrics(User actor) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        boolean managerScope = actor != null && actor.getRole() != null && "MANAGER".equals(actor.getRole().name());

        List<User> users = managerScope && actor.getDepartment() != null
                ? userRepository.findByDepartment(actor.getDepartment())
                : userRepository.findAll();
        List<Integer> scopedUserIds = users.stream().map(User::getId).filter(Objects::nonNull).toList();

        List<TaskAssignment> tasks = taskAssignmentRepository.findAllWithUser().stream()
                .filter(t -> !managerScope || t.getUser() == null || scopedUserIds.contains(t.getUser().getId()))
                .toList();
        List<KpiGoal> kpis = kpiGoalRepository.findAll().stream()
                .filter(k -> !managerScope || k.getUser() == null || scopedUserIds.contains(k.getUser().getId()))
                .toList();

        long activeUsers = users.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long pendingLeaves = managerScope && actor.getDepartment() != null
                ? leaveRequestRepository.countPendingByDepartment(actor.getDepartment())
                : leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long pendingTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long activeKpis = kpis.stream().filter(k -> k.getStatus() == KpiStatus.ACTIVE).count();
        long completedKpis = kpis.stream().filter(k -> k.getStatus() == KpiStatus.COMPLETED).count();

        metrics.put("employeesTotal", users.size());
        metrics.put("employeesActive", activeUsers);
        metrics.put("leavePending", pendingLeaves);
        metrics.put("tasksTotal", tasks.size());
        metrics.put("tasksPending", pendingTasks);
        metrics.put("tasksInProgress", inProgressTasks);
        metrics.put("tasksCompleted", completedTasks);
        metrics.put("taskCompletionRate", percent(completedTasks, tasks.size()));
        metrics.put("kpisTotal", kpis.size());
        metrics.put("kpisActive", activeKpis);
        metrics.put("kpisCompleted", completedKpis);
        metrics.put("kpiCompletionRate", percent(completedKpis, kpis.size()));
        return metrics;
    }

    private List<String> buildLocalRecommendations(Map<String, Object> metrics) {
        List<String> recommendations = new ArrayList<>();
        long pendingLeaves = asLong(metrics.get("leavePending"));
        long pendingTasks = asLong(metrics.get("tasksPending"));
        long inProgressTasks = asLong(metrics.get("tasksInProgress"));
        BigDecimal taskCompletionRate = (BigDecimal) metrics.get("taskCompletionRate");
        BigDecimal kpiCompletionRate = (BigDecimal) metrics.get("kpiCompletionRate");

        if (pendingLeaves > 0) {
            recommendations.add("Review pending leave requests to avoid payroll and staffing delays.");
        }
        if (pendingTasks > inProgressTasks) {
            recommendations.add("Move blocked or pending tasks into active ownership with clear assignees.");
        }
        if (taskCompletionRate.compareTo(BigDecimal.valueOf(60)) < 0) {
            recommendations.add("Check task workload and overdue work because completion rate is below 60%.");
        }
        if (kpiCompletionRate.compareTo(BigDecimal.valueOf(50)) < 0) {
            recommendations.add("Review active KPI progress and ask managers to update current values.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Maintain current operating rhythm and keep KPI/task data up to date.");
        }
        return recommendations;
    }

    private String buildLocalSummary(String scope, Map<String, Object> metrics, List<String> recommendations) {
        return "AI HR insight for " + scope
                + ": employees=" + metrics.get("employeesTotal")
                + ", pendingLeaves=" + metrics.get("leavePending")
                + ", taskCompletionRate=" + metrics.get("taskCompletionRate") + "%"
                + ", kpiCompletionRate=" + metrics.get("kpiCompletionRate") + "%. "
                + "Top action: " + recommendations.get(0);
    }

    private String buildSystemPrompt() {
        return """
                You are an HR operations analyst inside an HR management system.
                Summarize only the provided metrics. Do not invent employee names or private data.
                Return concise Vietnamese without markdown, focused on risks and next actions.
                """;
    }

    private String buildUserPrompt(String scope, Map<String, Object> metrics) {
        return "Scope: " + scope + "\nMetrics: " + metrics;
    }

    private String buildAdvisorSystemPrompt() {
        return """
                You are an HR operations advisor inside an HR management system.
                Answer in concise Vietnamese. Use only the provided metrics and recommendations.
                Do not invent employee names, salaries, medical facts, or private details.
                Give practical next actions for an admin or manager.
                """;
    }

    private String buildAdvisorUserPrompt(String scope, Map<String, Object> metrics,
                                          List<String> recommendations, String question) {
        return "Scope: " + scope
                + "\nMetrics: " + metrics
                + "\nCurrent recommendations: " + recommendations
                + "\nQuestion: " + question;
    }

    private String buildLocalAdvisorAnswer(String scope, Map<String, Object> metrics,
                                           List<String> recommendations, String question) {
        if (question == null || question.isBlank()) {
            return "Hay nhap cau hoi ve van hanh nhan su, vi du: uu tien xu ly gi hom nay, KPI co rui ro khong, hoac task dang bi nghe o dau.";
        }

        String normalized = question.toLowerCase();
        if (normalized.contains("kpi")) {
            return "Trong " + scope + ", ty le hoan thanh KPI hien la " + metrics.get("kpiCompletionRate")
                    + "%. Uu tien ra soat KPI dang active (" + metrics.get("kpisActive")
                    + ") va yeu cau cap nhat current value cho cac KPI qua han.";
        }
        if (normalized.contains("task") || normalized.contains("cong viec")) {
            return "Trong " + scope + ", task pending=" + metrics.get("tasksPending")
                    + ", dang lam=" + metrics.get("tasksInProgress")
                    + ", hoan thanh=" + metrics.get("tasksCompleted")
                    + ". Nen chot owner cho task pending va tach viec bi block thanh action nho hon.";
        }
        if (normalized.contains("nghi") || normalized.contains("leave") || normalized.contains("phep")) {
            return "Dang co " + metrics.get("leavePending")
                    + " don nghi phep cho duyet. Nen xu ly truoc cac don anh huong lich truc, payroll hoac nhan su thay the.";
        }

        return "Tom tat cho " + scope + ": nhan vien=" + metrics.get("employeesTotal")
                + ", don nghi cho duyet=" + metrics.get("leavePending")
                + ", task completion=" + metrics.get("taskCompletionRate")
                + "%, KPI completion=" + metrics.get("kpiCompletionRate")
                + "%. Hanh dong uu tien: " + recommendations.get(0);
    }

    private BigDecimal percent(long value, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }
}


