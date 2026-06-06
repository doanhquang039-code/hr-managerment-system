package com.example.hr.service;


import com.example.hr.payroll.entity.Payroll;
import com.example.hr.dto.AiAgentResponse;
import com.example.hr.enums.KpiStatus;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.enums.TaskStatus;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.Department;
import com.example.hr.models.KpiGoal;
import com.example.hr.models.Task;
import com.example.hr.models.TaskAssignment;
import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.EmployeeWarningRepository;
import com.example.hr.repository.KpiGoalRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.repository.TaskAssignmentRepository;
import com.example.hr.repository.TaskRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class AiAgentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private KpiGoalRepository kpiGoalRepository;

    @Autowired
    private EmployeeWarningRepository warningRepository;

    @Autowired(required = false)
    private GeminiAiService geminiAiService;

    @Transactional(readOnly = true)
    public AiAgentResponse run(User actor, String rawQuery, String requestedMode) {
        String query = rawQuery != null ? rawQuery.trim() : "";
        String intent = resolveIntent(query, requestedMode);
        String scope = resolveScope(actor);
        AgentContext context = buildContext(actor);

        List<Map<String, Object>> findings = diagnose(context);
        List<Map<String, Object>> searchResults = intent.equals("SEARCH") || intent.equals("AGENT")
                ? search(context, query)
                : List.of();
        int healthScore = calculateHealthScore(findings, context);
        Map<String, Object> dataProfile = buildDataProfile(context, findings, searchResults);
        List<String> nextActions = buildNextActions(findings, context);
        List<String> tools = new ArrayList<>();
        tools.add("hr-analytics");
        tools.add("risk-diagnosis");
        if (!searchResults.isEmpty()) {
            tools.add("internal-search");
        }

        List<String> models = new ArrayList<>();
        models.add("hr-risk-rule-v1");
        models.add("hr-intent-router-v1");

        String mode = "rule-based-agent";
        String answer = buildLocalAnswer(intent, scope, query, context, findings, searchResults, nextActions, healthScore);
        if (geminiAiService != null && geminiAiService.isAvailable() && !query.isBlank()) {
            String aiAnswer = geminiAiService.chat(buildSystemPrompt(), buildUserPrompt(intent, scope, query, context, findings, searchResults, nextActions, healthScore, dataProfile));
            if (StringUtils.hasText(aiAnswer)) {
                mode = "gemini-agent";
                models.add(geminiAiService.getModel());
                answer = aiAnswer.trim();
            }
        }

        return AiAgentResponse.builder()
                .intent(intent)
                .scope(scope)
                .mode(mode)
                .answer(answer)
                .healthScore(healthScore)
                .dataProfile(dataProfile)
                .modelsUsed(models)
                .toolsUsed(tools)
                .findings(findings)
                .searchResults(searchResults)
                .nextActions(nextActions)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private AgentContext buildContext(User actor) {
        boolean managerScope = actor != null && actor.getRole() != null && "MANAGER".equals(actor.getRole().name());
        Department department = managerScope ? actor.getDepartment() : null;
        List<User> users = department != null ? userRepository.findByDepartment(department) : userRepository.findAll();
        List<Integer> userIds = users.stream().map(User::getId).filter(Objects::nonNull).toList();

        List<TaskAssignment> assignments = taskAssignmentRepository.findAllWithUser().stream()
                .filter(a -> !managerScope || a.getUser() == null || userIds.contains(a.getUser().getId()))
                .toList();
        List<KpiGoal> kpis = kpiGoalRepository.findAll().stream()
                .filter(k -> !managerScope || k.getUser() == null || userIds.contains(k.getUser().getId()))
                .toList();

        long activeUsers = users.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long pendingLeaves = department != null
                ? leaveRequestRepository.countPendingByDepartment(department)
                : leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long pendingTasks = assignments.stream().filter(a -> a.getStatus() == TaskStatus.PENDING).count();
        long inProgressTasks = assignments.stream().filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completedTasks = assignments.stream().filter(a -> a.getStatus() == TaskStatus.COMPLETED).count();
        long activeKpis = kpis.stream().filter(k -> k.getStatus() == KpiStatus.ACTIVE).count();
        long completedKpis = kpis.stream().filter(k -> k.getStatus() == KpiStatus.COMPLETED).count();
        long severeWarnings = warningRepository.countEmployeesWithSevereWarnings(LocalDate.now());

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("employeesTotal", users.size());
        metrics.put("employeesActive", activeUsers);
        metrics.put("leavePending", pendingLeaves);
        metrics.put("tasksTotal", assignments.size());
        metrics.put("tasksPending", pendingTasks);
        metrics.put("tasksInProgress", inProgressTasks);
        metrics.put("tasksCompleted", completedTasks);
        metrics.put("taskCompletionRate", percent(completedTasks, assignments.size()));
        metrics.put("kpisTotal", kpis.size());
        metrics.put("kpisActive", activeKpis);
        metrics.put("kpisCompleted", completedKpis);
        metrics.put("kpiCompletionRate", percent(completedKpis, kpis.size()));
        metrics.put("severeWarningEmployees", severeWarnings);

        return new AgentContext(managerScope, department, users, assignments, kpis, metrics);
    }

    private List<Map<String, Object>> diagnose(AgentContext context) {
        List<Map<String, Object>> findings = new ArrayList<>();
        BigDecimal taskCompletion = (BigDecimal) context.metrics().get("taskCompletionRate");
        BigDecimal kpiCompletion = (BigDecimal) context.metrics().get("kpiCompletionRate");
        long pendingLeaves = asLong(context.metrics().get("leavePending"));
        long pendingTasks = asLong(context.metrics().get("tasksPending"));
        long inProgressTasks = asLong(context.metrics().get("tasksInProgress"));
        long severeWarnings = asLong(context.metrics().get("severeWarningEmployees"));

        if (taskCompletion.compareTo(BigDecimal.valueOf(60)) < 0 && asLong(context.metrics().get("tasksTotal")) > 0) {
            findings.add(finding("TASK_DELIVERY_RISK", "HIGH", "Ty le hoan thanh task duoi 60%", "taskCompletionRate", taskCompletion,
                    "Task completion < 60% khi he thong da co task", 0.86));
        }
        if (pendingTasks > inProgressTasks) {
            findings.add(finding("TASK_BACKLOG", "MEDIUM", "Task pending nhieu hon task dang lam", "tasksPending", pendingTasks,
                    "tasksPending > tasksInProgress", 0.78));
        }
        if (kpiCompletion.compareTo(BigDecimal.valueOf(50)) < 0 && asLong(context.metrics().get("kpisTotal")) > 0) {
            findings.add(finding("KPI_PROGRESS_RISK", "HIGH", "Ty le hoan thanh KPI duoi 50%", "kpiCompletionRate", kpiCompletion,
                    "KPI completion < 50% khi he thong da co KPI", 0.84));
        }
        if (pendingLeaves > 0) {
            findings.add(finding("LEAVE_APPROVAL_QUEUE", "MEDIUM", "Con don nghi phep cho duyet", "leavePending", pendingLeaves,
                    "leavePending > 0", 0.72));
        }
        if (severeWarnings > 0) {
            findings.add(finding("EMPLOYEE_RELATION_RISK", "HIGH", "Co nhan vien dang co canh bao muc nang", "severeWarningEmployees", severeWarnings,
                    "severeWarningEmployees > 0", 0.9));
        }
        if (findings.isEmpty()) {
            findings.add(finding("OPERATING_NORMAL", "LOW", "Chua phat hien rui ro lon tu metric hien tai", "health", "stable",
                    "Khong co rule rui ro nao vuot nguong", 0.65));
        }
        return findings;
    }

    private List<Map<String, Object>> search(AgentContext context, String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        String keyword = extractSearchKeyword(query);
        if (keyword.length() < 2) {
            return List.of();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        context.users().stream()
                .filter(u -> contains(u.getFullName(), keyword)
                        || contains(u.getEmail(), keyword)
                        || contains(u.getEmployeeCode(), keyword))
                .limit(5)
                .forEach(u -> results.add(result("employee", u.getFullName(), u.getEmail(), "/admin/users/edit/" + u.getId())));

        departmentRepository.findAll().stream()
                .filter(d -> !context.managerScope() || Objects.equals(d.getId(), context.department() != null ? context.department().getId() : null))
                .filter(d -> contains(d.getDepartmentName(), keyword))
                .limit(3)
                .forEach(d -> results.add(result("department", d.getDepartmentName(), "Department", "/admin/departments")));

        taskRepository.findAll().stream()
                .filter(t -> contains(t.getTaskName(), keyword) || contains(t.getDescription(), keyword))
                .sorted(Comparator.comparing(Task::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(t -> results.add(result("task", t.getTaskName(), t.getTaskType() != null ? t.getTaskType().name() : "Task", "/admin/tasks")));

        return results;
    }

    private List<String> buildNextActions(List<Map<String, Object>> findings, AgentContext context) {
        List<String> actions = new ArrayList<>();
        for (Map<String, Object> finding : findings) {
            String code = String.valueOf(finding.get("code"));
            switch (code) {
                case "TASK_DELIVERY_RISK", "TASK_BACKLOG" ->
                        actions.add("Mo danh sach task, gan owner ro rang va cap nhat trang thai cac task pending.");
                case "KPI_PROGRESS_RISK" ->
                        actions.add("Ra soat KPI active, cap nhat current value va chot deadline cho KPI cham.");
                case "LEAVE_APPROVAL_QUEUE" ->
                        actions.add("Duyet hoac tu choi don nghi phep pending de tranh tre payroll/lich truc.");
                case "EMPLOYEE_RELATION_RISK" ->
                        actions.add("Kiem tra danh sach warning muc nang va len lich trao doi voi quan ly truc tiep.");
                default -> actions.add("Tiep tuc theo doi metric hang ngay va giu du lieu HR duoc cap nhat.");
            }
        }
        return actions.stream().distinct().limit(5).toList();
    }

    private String buildLocalAnswer(String intent, String scope, String query, AgentContext context,
                                    List<Map<String, Object>> findings,
                                    List<Map<String, Object>> searchResults,
                                    List<String> nextActions,
                                    int healthScore) {
        if (!StringUtils.hasText(query)) {
            return "AI Agent san sang. Ban co the yeu cau: phan tich du lieu HR, chan doan rui ro KPI/task/nghi phep, hoac tim kiem nhan vien/phong ban/task.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("AI Agent da xu ly intent ").append(intent).append(" cho ").append(scope).append(". ");
        sb.append("Health score hien tai: ").append(healthScore).append("/100. ");
        sb.append("Metric chinh: nhan vien=").append(context.metrics().get("employeesTotal"))
                .append(", task completion=").append(context.metrics().get("taskCompletionRate")).append("%")
                .append(", KPI completion=").append(context.metrics().get("kpiCompletionRate")).append("%")
                .append(", leave pending=").append(context.metrics().get("leavePending")).append(". ");
        if (!findings.isEmpty()) {
            sb.append("Chan doan uu tien: ").append(findings.get(0).get("message")).append(". ");
        }
        if (!searchResults.isEmpty()) {
            sb.append("Tim thay ").append(searchResults.size()).append(" ket qua lien quan. ");
        }
        if (!nextActions.isEmpty()) {
            sb.append("Hanh dong tiep theo: ").append(nextActions.get(0));
        }
        return sb.toString();
    }

    private String buildSystemPrompt() {
        return """
                You are an AI Agent for an HR management system.
                You can synthesize internal search results, HR analytics, and risk diagnosis.
                Answer in concise Vietnamese. Do not invent private data. Mention only provided metrics/results.
                Return practical diagnosis and next actions.
                """;
    }

    private String buildUserPrompt(String intent, String scope, String query, AgentContext context,
                                   List<Map<String, Object>> findings,
                                   List<Map<String, Object>> searchResults,
                                   List<String> nextActions,
                                   int healthScore,
                                   Map<String, Object> dataProfile) {
        return "Intent: " + intent
                + "\nScope: " + scope
                + "\nUser query: " + query
                + "\nHealth score: " + healthScore
                + "\nData profile: " + dataProfile
                + "\nMetrics: " + context.metrics()
                + "\nFindings: " + findings
                + "\nSearch results: " + searchResults
                + "\nNext actions: " + nextActions;
    }

    private String resolveIntent(String query, String requestedMode) {
        String mode = requestedMode != null ? requestedMode.trim().toUpperCase(Locale.ROOT) : "";
        if (List.of("SEARCH", "ANALYZE", "DIAGNOSE", "AGENT").contains(mode)) {
            return mode;
        }
        String q = normalizeQuery(query);
        if (q.contains("tim") || q.contains("search") || q.contains("kiem")) return "SEARCH";
        if (q.contains("chan doan") || q.contains("rui ro") || q.contains("risk") || q.contains("ket qua")) return "DIAGNOSE";
        if (q.contains("phan tich") || q.contains("analytics") || q.contains("thong ke")) return "ANALYZE";
        return "AGENT";
    }

    private String resolveScope(User actor) {
        if (actor != null && actor.getRole() != null && "MANAGER".equals(actor.getRole().name())) {
            String dept = actor.getDepartment() != null ? actor.getDepartment().getDepartmentName() : "unassigned department";
            return "department:" + dept;
        }
        return "organization";
    }

    private int calculateHealthScore(List<Map<String, Object>> findings, AgentContext context) {
        int score = 100;
        for (Map<String, Object> finding : findings) {
            String severity = String.valueOf(finding.get("severity"));
            if ("HIGH".equals(severity)) {
                score -= 18;
            } else if ("MEDIUM".equals(severity)) {
                score -= 9;
            } else if ("LOW".equals(severity) && !"OPERATING_NORMAL".equals(String.valueOf(finding.get("code")))) {
                score -= 3;
            }
        }
        if (asLong(context.metrics().get("employeesActive")) == 0 && asLong(context.metrics().get("employeesTotal")) > 0) {
            score -= 20;
        }
        return Math.max(0, Math.min(100, score));
    }

    private Map<String, Object> buildDataProfile(AgentContext context,
                                                 List<Map<String, Object>> findings,
                                                 List<Map<String, Object>> searchResults) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("recordsEmployees", context.users().size());
        profile.put("recordsTaskAssignments", context.assignments().size());
        profile.put("recordsKpis", context.kpis().size());
        profile.put("findingsCount", findings.size());
        profile.put("searchResultsCount", searchResults.size());
        profile.put("dataFreshness", "database-current");
        profile.put("scopeLimitedToDepartment", context.managerScope());
        return profile;
    }

    private Map<String, Object> finding(String code, String severity, String message, String metric, Object value,
                                        String evidence, double confidence) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("severity", severity);
        out.put("message", message);
        out.put("metric", metric);
        out.put("value", value);
        out.put("evidence", evidence);
        out.put("confidence", confidence);
        return out;
    }

    private Map<String, Object> result(String type, String title, String subtitle, String url) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", type);
        out.put("title", title != null ? title : "");
        out.put("subtitle", subtitle != null ? subtitle : "");
        out.put("url", url);
        return out;
    }

    private boolean contains(String value, String keyword) {
        return value != null && normalizeQuery(value).contains(keyword);
    }

    private String normalizeQuery(String value) {
        if (value == null) {
            return "";
        }
        String t = value.toLowerCase(Locale.ROOT).trim();
        t = t.replace('đ', 'd');
        return java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private String extractSearchKeyword(String value) {
        String normalized = normalizeQuery(value);
        return normalized
                .replace("tim kiem", " ")
                .replace("tim", " ")
                .replace("search", " ")
                .replace("noi bo", " ")
                .replace("nhan vien", " ")
                .replace("phong ban", " ")
                .replace("department", " ")
                .replace("task", " ")
                .replace("cong viec", " ")
                .replaceAll("\\s+", " ")
                .trim();
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

    private record AgentContext(boolean managerScope,
                                Department department,
                                List<User> users,
                                List<TaskAssignment> assignments,
                                List<KpiGoal> kpis,
                                Map<String, Object> metrics) {
    }
}
