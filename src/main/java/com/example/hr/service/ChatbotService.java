package com.example.hr.service;






import com.example.hr.payroll.entity.Payroll;
import com.example.hr.payroll.repository.PayrollRepository;
import com.example.hr.leave.repository.LeaveRequestRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.dto.ChatbotChatResponse;
import com.example.hr.enums.LeaveStatus;
import com.example.hr.models.ChatbotMessage;
import com.example.hr.models.User;
import com.example.hr.repository.*;
import com.example.hr.task.entity.TaskAssignment;
import com.example.hr.task.repository.TaskAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatbotService {

    @Autowired
    private ChatbotMessageRepository chatbotMessageRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private KpiGoalRepository kpiGoalRepository;

    /** Gemini AI â€” optional, null náº¿u chÆ°a cáº¥u hÃ¬nh */
    @Autowired(required = false)
    private GeminiAiService geminiAiService;

    @Autowired
    private HealthInsightService healthInsightService;

    @Transactional
    public ChatbotChatResponse chat(User user, String rawMessage, String sessionIdIn) {
        String message = rawMessage != null ? rawMessage.trim() : "";
        String sessionId = StringUtils.hasText(sessionIdIn) ? sessionIdIn.trim() : UUID.randomUUID().toString();

        if (!StringUtils.hasText(message)) {
            return saveAndBuild(user, sessionId, message, "EMPTY",
                    "Báº¡n hÃ£y nháº­p cÃ¢u há»i (vÃ­ dá»¥: cÃ¡ch xin nghá»‰ phÃ©p, xem lÆ°Æ¡ngâ€¦).", false);
        }

        String norm = normalize(message);
        boolean escalate = wantsEscalation(norm, message);

        String intent;
        String reply;

        if (matches(norm, "tien do cong viec", "tiáº¿n Ä‘á»™ cÃ´ng viá»‡c", "cong viec toi dau", "cÃ´ng viá»‡c tá»›i Ä‘Ã¢u", "task progress", "ket qua cong viec", "káº¿t quáº£ cÃ´ng viá»‡c")) {
            intent = "WORK_PROGRESS";
            reply = workProgressAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "ket qua kpi", "káº¿t quáº£ kpi", "ket qua", "káº¿t quáº£", "kpi toi dau", "kpi tá»›i Ä‘Ã¢u", "danh gia toi dau", "Ä‘Ã¡nh giÃ¡ tá»›i Ä‘Ã¢u")) {
            intent = "KPI_RESULT";
            reply = kpiResultAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "tinh hinh team", "tÃ¬nh hÃ¬nh team", "tien do muc tieu", "tiáº¿n Ä‘á»™ má»¥c tiÃªu", "muc tieu team", "má»¥c tiÃªu team")) {
            intent = "TEAM_PROGRESS";
            reply = workProgressAnswer(user) + "\n" + kpiResultAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "ngan sach", "ngÃ¢n sÃ¡ch", "budget")) {
            intent = "BUDGET_INFO";
            reply = "NgÃ¢n sÃ¡ch team xem táº¡i /manager/budget. BÃ¡o cÃ¡o ngÃ¢n sÃ¡ch xem táº¡i /manager/reports/budget.";
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "pipeline tuyen dung", "pipeline tuyá»ƒn dá»¥ng", "lich phong van", "lá»‹ch phá»ng váº¥n", "ung vien moi", "á»©ng viÃªn má»›i")) {
            intent = "HIRING_PROGRESS";
            reply = "Tiáº¿n Ä‘á»™ tuyá»ƒn dá»¥ng xem táº¡i /hiring/dashboard. Pipeline á»©ng viÃªn á»Ÿ /hiring/candidates, lá»‹ch phá»ng váº¥n á»Ÿ /hiring/interviews.";
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "don nghi cho duyet", "Ä‘Æ¡n nghá»‰ chá» duyá»‡t", "don nghi cua toi", "Ä‘Æ¡n nghá»‰ cá»§a tÃ´i")) {
            intent = "LEAVE_STATUS";
            reply = leaveAnswer(user, norm);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "suc khoe", "sá»©c khá»e", "stress", "met moi", "má»‡t má»i", "burnout", "ngu it", "ngá»§ Ã­t")) {
            intent = "HEALTH_INSIGHT";
            reply = healthInsightAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        // ===== TRY AI FIRST (náº¿u Gemini Ä‘Ã£ cáº¥u hÃ¬nh) =====
        if (geminiAiService != null && !escalate) {
            String aiReply = tryGeminiReply(user, message, norm);
            if (aiReply != null && !aiReply.isBlank()) {
                intent = "AI_GEMINI";
                return saveAndBuild(user, sessionId, message, intent, aiReply, false);
            }
        }

        // ===== FALLBACK: Rule-based =====

        if (escalate) {
            intent = "ESCALATE_HR";
            reply = "MÃ¬nh Ä‘Ã£ ghi nháº­n yÃªu cáº§u chuyá»ƒn cho bá»™ pháº­n nhÃ¢n sá»±. Báº¡n cÃ³ thá»ƒ gá»­i thÃªm chi tiáº¿t qua email cÃ´ng ty hoáº·c trao Ä‘á»•i trá»±c tiáº¿p HR. "
                    + "Trong há»‡ thá»‘ng: xem má»¥c ThÃ´ng bÃ¡o cÃ´ng ty hoáº·c liÃªn há»‡ quáº£n lÃ½ trá»±c tiáº¿p.";
        } else if (matches(norm, "xin chÃ o", "chÃ o", "hello", "hey")) {
            intent = "GREETING";
            reply = "ChÃ o " + (user != null && user.getFullName() != null ? user.getFullName() : "báº¡n")
                    + "! MÃ¬nh lÃ  trá»£ lÃ½ HR ná»™i bá»™ â€” há»i vá» nghá»‰ phÃ©p, lÆ°Æ¡ng, cháº¥m cÃ´ng, cÃ´ng viá»‡c hoáº·c KPI nhÃ©.";
        } else if (matches(norm, "nghá»‰ phÃ©p", "xin nghá»‰", "leave", "phÃ©p nÄƒm", "Ä‘Æ¡n nghá»‰")) {
            intent = "LEAVE_POLICY";
            reply = leaveAnswer(user, norm);
        } else if (matches(norm, "lÆ°Æ¡ng", "phiáº¿u lÆ°Æ¡ng", "payroll", "thÆ°á»Ÿng", "kháº¥u trá»«")) {
            intent = "PAYROLL_INFO";
            reply = payrollAnswer(user);
        } else if (matches(norm, "cháº¥m cÃ´ng", "check in", "checkin", "checkout", "Ä‘i muá»™n", "attendance")) {
            intent = "ATTENDANCE_INFO";
            reply = "Báº¡n cháº¥m cÃ´ng táº¡i trang Cháº¥m cÃ´ng (check-in / check-out theo ngÃ y). "
                    + "ÄÆ°á»ng dáº«n: /user/attendance. Náº¿u quÃªn cháº¥m, bÃ¡o quáº£n lÃ½ hoáº·c HR Ä‘á»ƒ xá»­ lÃ½ ngoáº¡i lá»‡.";
        } else if (matches(norm, "cÃ´ng viá»‡c", "task", "nhiá»‡m vá»¥", "phÃ¢n cÃ´ng")) {
            intent = "TASKS_INFO";
            reply = "CÃ´ng viá»‡c Ä‘Æ°á»£c giao náº±m táº¡i CÃ´ng viá»‡c cá»§a tÃ´i (/user1/tasks). Cáº­p nháº­t tráº¡ng thÃ¡i khi hoÃ n thÃ nh Ä‘á»ƒ quáº£n lÃ½ theo dÃµi.";
        } else if (matches(norm, "kpi", "Ä‘Ã¡nh giÃ¡", "review", "thÃ nh tÃ­ch")) {
            intent = "REVIEWS_INFO";
            reply = "Káº¿t quáº£ Ä‘Ã¡nh giÃ¡ KPI xem táº¡i ÄÃ¡nh giÃ¡ KPI (/user1/reviews). Má»i tháº¯c máº¯c vá» tiÃªu chÃ­, hÃ£y trao Ä‘á»•i vá»›i quáº£n lÃ½ trá»±c tiáº¿p.";
        } else if (matches(norm, "thÃ´ng bÃ¡o", "announcement", "tin cÃ´ng ty")) {
            intent = "ANNOUNCEMENTS";
            reply = "ThÃ´ng bÃ¡o ná»™i bá»™ táº¡i ThÃ´ng bÃ¡o cÃ´ng ty (/user1/announcements).";
        } else if (matches(norm, "notification", "chuÃ´ng", "tin nháº¯n há»‡ thá»‘ng")) {
            intent = "NOTIFICATIONS";
            reply = "ThÃ´ng bÃ¡o trong á»©ng dá»¥ng xem táº¡i `/notifications` (menu thÃ´ng bÃ¡o).";
        } else {
            intent = "UNKNOWN";
            reply = "MÃ¬nh chÆ°a hiá»ƒu cÃ¢u há»i nÃ y. Báº¡n thá»­ há»i vá»: nghá»‰ phÃ©p, lÆ°Æ¡ng, cháº¥m cÃ´ng, cÃ´ng viá»‡c, KPI, thÃ´ng bÃ¡o â€” "
                    + "hoáº·c gÃµ Â«gáº·p nhÃ¢n sá»±Â» / Â«chuyá»ƒn HRÂ» Ä‘á»ƒ Ä‘Æ°á»£c há»— trá»£ trá»±c tiáº¿p.";
        }

        return saveAndBuild(user, sessionId, message, intent, reply, escalate);
    }

    private ChatbotChatResponse saveAndBuild(User user, String sessionId, String userQuery, String intent,
                                             String reply, boolean escalated) {
        ChatbotMessage row = new ChatbotMessage();
        row.setUser(user);
        row.setSessionId(sessionId);
        row.setUserQuery(userQuery);
        row.setBotResponse(reply);
        row.setIntent(intent);
        row.setIsEscalated(escalated);
        row.setCreatedAt(LocalDateTime.now());
        chatbotMessageRepository.save(row);

        return ChatbotChatResponse.builder()
                .messageId(row.getId())
                .sessionId(sessionId)
                .intent(intent)
                .reply(reply)
                .escalated(escalated)
                .build();
    }

    @Transactional
    public boolean rateMessage(User user, int messageId, int rating) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        return chatbotMessageRepository.findByIdAndUser(messageId, user)
                .map(msg -> {
                    msg.setRating(rating);
                    chatbotMessageRepository.save(msg);
                    return true;
                })
                .orElse(false);
    }

    private String leaveAnswer(User user, String norm) {
        String base = "ÄÆ¡n nghá»‰ phÃ©p gá»­i táº¡i Xin nghá»‰ phÃ©p (/user/leaves). HR hoáº·c quáº£n lÃ½ sáº½ duyá»‡t; khi cÃ³ káº¿t quáº£ báº¡n nháº­n thÃ´ng bÃ¡o trong há»‡ thá»‘ng.";
        if (user == null) {
            return base;
        }
        boolean askCount = norm.contains("pending") || norm.contains("cho duyet")
                || norm.contains("bao nhieu") || norm.contains("may don");
        long pending = leaveRequestRepository.findByUser(user).stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .count();
        if (askCount) {
            return base + " Hiá»‡n báº¡n cÃ³ " + pending + " Ä‘Æ¡n Ä‘ang chá» duyá»‡t.";
        }
        if (pending > 0) {
            return base + " (Gá»£i Ã½: báº¡n Ä‘ang cÃ³ " + pending + " Ä‘Æ¡n chá» duyá»‡t.)";
        }
        return base;
    }

    private String payrollAnswer(User user) {
        String base = "Phiáº¿u lÆ°Æ¡ng xem táº¡i Phiáº¿u lÆ°Æ¡ng (/user1/payroll). Tráº¡ng thÃ¡i thanh toÃ¡n Ä‘Æ°á»£c cáº­p nháº­t khi HR/Admin xá»­ lÃ½.";
        if (user == null) {
            return base;
        }
        int m = LocalDate.now().getMonthValue();
        int y = LocalDate.now().getYear();
        boolean has = payrollRepository.findByUserIdAndMonthAndYear(user.getId(), m, y).isPresent();
        if (has) {
            return base + " ThÃ¡ng " + m + "/" + y + ": há»‡ thá»‘ng Ä‘Ã£ cÃ³ báº£n ghi báº£ng lÆ°Æ¡ng cho báº¡n.";
        }
        return base + " ThÃ¡ng " + m + "/" + y + ": chÆ°a tháº¥y báº£n ghi lÆ°Æ¡ng â€” cÃ³ thá»ƒ HR chÆ°a táº¡o; liÃªn há»‡ HR náº¿u cáº§n gáº¥p.";
    }

    private String healthInsightAnswer(User user) {
        HealthInsightService.HealthInsightResult result = healthInsightService.analyze(
                user,
                new HealthInsightService.HealthInsightInput(7.0, 4, 6000, 1.8, 0.0, null)
        );
        return "MÃ¬nh Ä‘Ã£ thÃªm cÃ´ng cá»¥ Health insight trÃªn dashboard Ä‘á»ƒ báº¡n nháº­p ngá»§/stress/bÆ°á»›c chÃ¢n/nÆ°á»›c uá»‘ng/OT vÃ  nháº­n phÃ¢n tÃ­ch theo vai trÃ². "
                + "Máº·c Ä‘á»‹nh hiá»‡n táº¡i: " + result.wellnessScore() + "/100 - " + result.riskLevel() + ". "
                + result.summary() + " LÆ°u Ã½: Ä‘Ã¢y chá»‰ lÃ  tham kháº£o, khÃ´ng thay tháº¿ tÆ° váº¥n y táº¿.";
    }

    private String workProgressAnswer(User user) {
        if (user == null) {
            return "Báº¡n cáº§n Ä‘Äƒng nháº­p Ä‘á»ƒ xem tiáº¿n Ä‘á»™ cÃ´ng viá»‡c.";
        }

        String role = user.getRole() != null ? user.getRole().name() : "USER";
        if ("ADMIN".equals(role)) {
            return buildTaskSummary("ToÃ n há»‡ thá»‘ng", taskAssignmentRepository.findAllWithUser(), "/admin/tasks");
        }
        if ("MANAGER".equals(role)) {
            List<com.example.hr.task.entity.TaskAssignment> all = taskAssignmentRepository.findAllWithUser();
            Integer deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
            List<com.example.hr.task.entity.TaskAssignment> team = new ArrayList<>();
            for (com.example.hr.task.entity.TaskAssignment a : all) {
                if (a.getUser() != null && a.getUser().getDepartment() != null
                        && Objects.equals(a.getUser().getDepartment().getId(), deptId)) {
                    team.add(a);
                }
            }
            return buildTaskSummary("Team cá»§a báº¡n", team, "/manager/team");
        }
        if ("HIRING".equals(role)) {
            return "Tiáº¿n Ä‘á»™ tuyá»ƒn dá»¥ng xem táº¡i /hiring/dashboard, gá»“m pipeline á»©ng viÃªn, lá»‹ch phá»ng váº¥n vÃ  tin Ä‘Äƒng tuyá»ƒn dá»¥ng. Xem chi tiáº¿t á»Ÿ Candidates hoáº·c Interviews.";
        }

        return buildTaskSummary("CÃ´ng viá»‡c cá»§a báº¡n", taskAssignmentRepository.findByUser(user), "/user1/tasks");
    }

    private String buildTaskSummary(String scope, List<com.example.hr.task.entity.TaskAssignment> rows, String link) {
        int total = rows != null ? rows.size() : 0;
        long pending = 0;
        long inProgress = 0;
        long completed = 0;
        long canceled = 0;

        if (rows != null) {
            for (com.example.hr.task.entity.TaskAssignment a : rows) {
                String status = a.getStatus() != null ? a.getStatus().name() : "";
                if ("PENDING".equals(status)) pending++;
                else if ("IN_PROGRESS".equals(status)) inProgress++;
                else if ("COMPLETED".equals(status) || "DONE".equals(status)) completed++;
                else if ("CANCELED".equals(status)) canceled++;
            }
        }

        return scope + ": tá»•ng " + total + " cÃ´ng viá»‡c. "
                + "ChÆ°a báº¯t Ä‘áº§u: " + pending + ", Ä‘ang lÃ m: " + inProgress
                + ", hoÃ n thÃ nh: " + completed + ", Ä‘Ã£ há»§y: " + canceled + ". "
                + "Xem chi tiáº¿t táº¡i " + link + ".";
    }

    private String kpiResultAnswer(User user) {
        if (user == null) {
            return "Báº¡n cáº§n Ä‘Äƒng nháº­p Ä‘á»ƒ xem káº¿t quáº£ KPI.";
        }

        String role = user.getRole() != null ? user.getRole().name() : "USER";
        if ("ADMIN".equals(role)) {
            long active = kpiGoalRepository.findByStatus(com.example.hr.enums.KpiStatus.ACTIVE).size();
            long completed = kpiGoalRepository.findByStatus(com.example.hr.enums.KpiStatus.COMPLETED).size();
            return "KPI toÃ n há»‡ thá»‘ng: Ä‘ang thá»±c hiá»‡n " + active + ", Ä‘Ã£ hoÃ n thÃ nh " + completed
                    + ". Xem chi tiáº¿t táº¡i /admin/kpi.";
        }
        if ("MANAGER".equals(role) && user.getDepartment() != null) {
            List<com.example.hr.models.KpiGoal> goals = kpiGoalRepository.findByDepartmentId(user.getDepartment().getId());
            long active = goals.stream().filter(k -> k.getStatus() != null && "ACTIVE".equals(k.getStatus().name())).count();
            long completed = goals.stream().filter(k -> k.getStatus() != null && "COMPLETED".equals(k.getStatus().name())).count();
            return "KPI cá»§a team: tá»•ng " + goals.size() + ", Ä‘ang thá»±c hiá»‡n " + active + ", Ä‘Ã£ hoÃ n thÃ nh " + completed
                    + ". Xem thÃªm á»Ÿ /manager/performance.";
        }
        if ("HIRING".equals(role)) {
            return "Káº¿t quáº£ tuyá»ƒn dá»¥ng xem táº¡i /hiring/analytics/performance vÃ  /hiring/reports.";
        }

        List<com.example.hr.models.KpiGoal> activeGoals = kpiGoalRepository.findActiveGoalsByUser(user.getId(), LocalDate.now());
        Double avg = kpiGoalRepository.avgAchievementByUser(user.getId());
        return "KPI cá»§a báº¡n: Ä‘ang active " + activeGoals.size()
                + ", Ä‘iá»ƒm trung bÃ¬nh KPI Ä‘Ã£ hoÃ n thÃ nh: " + (avg != null ? String.format(Locale.ROOT, "%.1f%%", avg) : "chÆ°a cÃ³")
                + ". Xem chi tiáº¿t táº¡i /user1/kpi vÃ  /user1/reviews.";
    }

    private static boolean wantsEscalation(String norm, String raw) {
        return matches(norm, "gáº·p nhÃ¢n sá»±", "chuyen hr", "chuyá»ƒn hr", "lien he hr", "liÃªn há»‡ hr", "escalate", "hotline hr")
                || raw.toLowerCase(Locale.ROOT).contains("hr Æ¡i");
    }

    private static boolean matches(String norm, String... keys) {
        for (String k : keys) {
            if (norm.contains(normalize(k))) {
                return true;
            }
        }
        return false;
    }

    /** Chuáº©n hÃ³a nháº¹: thÆ°á»ng + bá» dáº¥u tiáº¿ng Viá»‡t cÆ¡ báº£n Ä‘á»ƒ khá»›p tá»« khÃ³a. */
    private static String normalize(String s) {
        String t = s.toLowerCase(Locale.ROOT).trim();
        t = t.replace('\u0111', 'd');
        t = java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return t;
    }

    // ==================== AI METHODS ====================

    /**
     * Gá»i Gemini vá»›i context HR cá»§a user hiá»‡n táº¡i.
     * Tráº£ vá» null náº¿u AI khÃ´ng available hoáº·c lá»—i.
     */
    private String tryGeminiReply(User user, String message, String norm) {
        if (geminiAiService == null) return null;
        try {
            String systemPrompt = buildSystemPrompt(user);
            String reply = geminiAiService.chat(systemPrompt, message);
            return reply;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * XÃ¢y dá»±ng system prompt vá»›i dá»¯ liá»‡u HR thá»±c táº¿ cá»§a user.
     */
    private String buildSystemPrompt(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Báº¡n lÃ  trá»£ lÃ½ HR thÃ´ng minh cá»§a há»‡ thá»‘ng HRMS. ");
        sb.append("Tráº£ lá»i ngáº¯n gá»n, chÃ­nh xÃ¡c báº±ng tiáº¿ng Viá»‡t. ");
        sb.append("Chá»‰ tráº£ lá»i cÃ¡c cÃ¢u há»i liÃªn quan Ä‘áº¿n HR, nhÃ¢n sá»±, cÃ´ng viá»‡c. ");
        sb.append("Náº¿u cÃ¢u há»i khÃ´ng liÃªn quan HR, tá»« chá»‘i lá»‹ch sá»±.\n\n");

        if (user != null) {
            sb.append("=== THÃ”NG TIN NHÃ‚N VIÃŠN ===\n");
            sb.append("TÃªn: ").append(user.getFullName()).append("\n");
            sb.append("Email: ").append(user.getEmail() != null ? user.getEmail() : "N/A").append("\n");
            if (user.getDepartment() != null) {
                sb.append("PhÃ²ng ban: ").append(user.getDepartment().getDepartmentName()).append("\n");
            }
            if (user.getPosition() != null) {
                sb.append("Chá»©c vá»¥: ").append(user.getPosition().getPositionName()).append("\n");
            }
            sb.append("Vai trÃ²: ").append(user.getRole()).append("\n");

            // Leave info
            try {
                long pendingLeaves = leaveRequestRepository.findByUser(user).stream()
                        .filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
                long approvedLeaves = leaveRequestRepository.findByUser(user).stream()
                        .filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
                sb.append("\n=== NGHá»ˆ PHÃ‰P ===\n");
                sb.append("ÄÆ¡n chá» duyá»‡t: ").append(pendingLeaves).append("\n");
                sb.append("ÄÆ¡n Ä‘Ã£ duyá»‡t: ").append(approvedLeaves).append("\n");
            } catch (Exception ignored) {}

            // Payroll info
            try {
                int m = LocalDate.now().getMonthValue();
                int y = LocalDate.now().getYear();
                payrollRepository.findByUserIdAndMonthAndYear(user.getId(), m, y).ifPresent(p -> {
                    sb.append("\n=== LÆ¯Æ NG THÃNG ").append(m).append("/").append(y).append(" ===\n");
                    sb.append("LÆ°Æ¡ng cÆ¡ báº£n: ").append(p.getBaseSalary()).append(" VND\n");
                    sb.append("Tráº¡ng thÃ¡i: ").append(p.getPaymentStatus()).append("\n");
                });
            } catch (Exception ignored) {}

            // Tasks
            try {
                long pendingTasks = taskAssignmentRepository.findByUser(user).stream()
                        .filter(t -> t.getStatus() != null && t.getStatus().name().equals("PENDING"))
                        .count();
                long inProgressTasks = taskAssignmentRepository.findByUser(user).stream()
                        .filter(t -> t.getStatus() != null && t.getStatus().name().equals("IN_PROGRESS"))
                        .count();
                sb.append("\n=== CÃ”NG VIá»†C ===\n");
                sb.append("Chá» thá»±c hiá»‡n: ").append(pendingTasks).append("\n");
                sb.append("Äang thá»±c hiá»‡n: ").append(inProgressTasks).append("\n");
            } catch (Exception ignored) {}

            // KPI
            try {
                long activeKpi = kpiGoalRepository.findByUserId(user.getId()).stream()
                        .filter(k -> k.getStatus() != null && k.getStatus().name().equals("ACTIVE"))
                        .count();
                if (activeKpi > 0) {
                    sb.append("\n=== KPI ===\n");
                    sb.append("KPI Ä‘ang active: ").append(activeKpi).append("\n");
                }
            } catch (Exception ignored) {}
        }

        sb.append("\n=== HÆ¯á»šNG DáºªN ===\n");
        sb.append("- Nghá»‰ phÃ©p: /user/leaves\n");
        sb.append("- Phiáº¿u lÆ°Æ¡ng: /user1/payroll\n");
        sb.append("- Cháº¥m cÃ´ng: /user/attendance\n");
        sb.append("- CÃ´ng viá»‡c: /user1/tasks\n");
        sb.append("- KPI: /user1/kpi\n");
        sb.append("- ThÃ´ng bÃ¡o: /notifications\n");
        sb.append("- TÃ i liá»‡u: /user1/documents\n");
        sb.append("- Chi phÃ­: /user1/expenses\n");

        return sb.toString();
    }
}


