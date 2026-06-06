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

    /** Gemini AI — optional, null nếu chưa cấu hình */
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
                    "Bạn hãy nhập câu hỏi (ví dụ: cách xin nghỉ phép, xem lương…).", false);
        }

        String norm = normalize(message);
        boolean escalate = wantsEscalation(norm, message);

        String intent;
        String reply;

        if (matches(norm, "tien do cong viec", "tiến độ công việc", "cong viec toi dau", "công việc tới đâu", "task progress", "ket qua cong viec", "kết quả công việc")) {
            intent = "WORK_PROGRESS";
            reply = workProgressAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "ket qua kpi", "kết quả kpi", "ket qua", "kết quả", "kpi toi dau", "kpi tới đâu", "danh gia toi dau", "đánh giá tới đâu")) {
            intent = "KPI_RESULT";
            reply = kpiResultAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "tinh hinh team", "tình hình team", "tien do muc tieu", "tiến độ mục tiêu", "muc tieu team", "mục tiêu team")) {
            intent = "TEAM_PROGRESS";
            reply = workProgressAnswer(user) + "\n" + kpiResultAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "ngan sach", "ngân sách", "budget")) {
            intent = "BUDGET_INFO";
            reply = "Ngân sách team xem tại /manager/budget. Báo cáo ngân sách xem tại /manager/reports/budget.";
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "pipeline tuyen dung", "pipeline tuyển dụng", "lich phong van", "lịch phỏng vấn", "ung vien moi", "ứng viên mới")) {
            intent = "HIRING_PROGRESS";
            reply = "Tiến độ tuyển dụng xem tại /hiring/dashboard. Pipeline ứng viên ở /hiring/candidates, lịch phỏng vấn ở /hiring/interviews.";
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "don nghi cho duyet", "đơn nghỉ chờ duyệt", "don nghi cua toi", "đơn nghỉ của tôi")) {
            intent = "LEAVE_STATUS";
            reply = leaveAnswer(user, norm);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        if (matches(norm, "suc khoe", "sức khỏe", "stress", "met moi", "mệt mỏi", "burnout", "ngu it", "ngủ ít")) {
            intent = "HEALTH_INSIGHT";
            reply = healthInsightAnswer(user);
            return saveAndBuild(user, sessionId, message, intent, reply, false);
        }

        // ===== TRY AI FIRST (nếu Gemini đã cấu hình) =====
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
            reply = "Mình đã ghi nhận yêu cầu chuyển cho bộ phận nhân sự. Bạn có thể gửi thêm chi tiết qua email công ty hoặc trao đổi trực tiếp HR. "
                    + "Trong hệ thống: xem mục Thông báo công ty hoặc liên hệ quản lý trực tiếp.";
        } else if (matches(norm, "xin chào", "chào", "hello", "hey")) {
            intent = "GREETING";
            reply = "Chào " + (user != null && user.getFullName() != null ? user.getFullName() : "bạn")
                    + "! Mình là trợ lý HR nội bộ — hỏi về nghỉ phép, lương, chấm công, công việc hoặc KPI nhé.";
        } else if (matches(norm, "nghỉ phép", "xin nghỉ", "leave", "phép năm", "đơn nghỉ")) {
            intent = "LEAVE_POLICY";
            reply = leaveAnswer(user, norm);
        } else if (matches(norm, "lương", "phiếu lương", "payroll", "thưởng", "khấu trừ")) {
            intent = "PAYROLL_INFO";
            reply = payrollAnswer(user);
        } else if (matches(norm, "chấm công", "check in", "checkin", "checkout", "đi muộn", "attendance")) {
            intent = "ATTENDANCE_INFO";
            reply = "Bạn chấm công tại trang Chấm công (check-in / check-out theo ngày). "
                    + "Đường dẫn: /user/attendance. Nếu quên chấm, báo quản lý hoặc HR để xử lý ngoại lệ.";
        } else if (matches(norm, "công việc", "task", "nhiệm vụ", "phân công")) {
            intent = "TASKS_INFO";
            reply = "Công việc được giao nằm tại Công việc của tôi (/user1/tasks). Cập nhật trạng thái khi hoàn thành để quản lý theo dõi.";
        } else if (matches(norm, "kpi", "đánh giá", "review", "thành tích")) {
            intent = "REVIEWS_INFO";
            reply = "Kết quả đánh giá KPI xem tại Đánh giá KPI (/user1/reviews). Mọi thắc mắc về tiêu chí, hãy trao đổi với quản lý trực tiếp.";
        } else if (matches(norm, "thông báo", "announcement", "tin công ty")) {
            intent = "ANNOUNCEMENTS";
            reply = "Thông báo nội bộ tại Thông báo công ty (/user1/announcements).";
        } else if (matches(norm, "notification", "chuông", "tin nhắn hệ thống")) {
            intent = "NOTIFICATIONS";
            reply = "Thông báo trong ứng dụng xem tại `/notifications` (menu thông báo).";
        } else {
            intent = "UNKNOWN";
            reply = "Mình chưa hiểu câu hỏi này. Bạn thử hỏi về: nghỉ phép, lương, chấm công, công việc, KPI, thông báo — "
                    + "hoặc gõ «gặp nhân sự» / «chuyển HR» để được hỗ trợ trực tiếp.";
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
        String base = "Đơn nghỉ phép gửi tại Xin nghỉ phép (/user/leaves). HR hoặc quản lý sẽ duyệt; khi có kết quả bạn nhận thông báo trong hệ thống.";
        if (user == null) {
            return base;
        }
        boolean askCount = norm.contains("pending") || norm.contains("cho duyet")
                || norm.contains("bao nhieu") || norm.contains("may don");
        long pending = leaveRequestRepository.findByUser(user).stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .count();
        if (askCount) {
            return base + " Hiện bạn có " + pending + " đơn đang chờ duyệt.";
        }
        if (pending > 0) {
            return base + " (Gợi ý: bạn đang có " + pending + " đơn chờ duyệt.)";
        }
        return base;
    }

    private String payrollAnswer(User user) {
        String base = "Phiếu lương xem tại Phiếu lương (/user1/payroll). Trạng thái thanh toán được cập nhật khi HR/Admin xử lý.";
        if (user == null) {
            return base;
        }
        int m = LocalDate.now().getMonthValue();
        int y = LocalDate.now().getYear();
        boolean has = payrollRepository.findByUserIdAndMonthAndYear(user.getId(), m, y).isPresent();
        if (has) {
            return base + " Tháng " + m + "/" + y + ": hệ thống đã có bản ghi bảng lương cho bạn.";
        }
        return base + " Tháng " + m + "/" + y + ": chưa thấy bản ghi lương — có thể HR chưa tạo; liên hệ HR nếu cần gấp.";
    }

    private String healthInsightAnswer(User user) {
        HealthInsightService.HealthInsightResult result = healthInsightService.analyze(
                user,
                new HealthInsightService.HealthInsightInput(7.0, 4, 6000, 1.8, 0.0, null)
        );
        return "Mình đã thêm công cụ Health insight trên dashboard để bạn nhập ngủ/stress/bước chân/nước uống/OT và nhận phân tích theo vai trò. "
                + "Mặc định hiện tại: " + result.wellnessScore() + "/100 - " + result.riskLevel() + ". "
                + result.summary() + " Lưu ý: đây chỉ là tham khảo, không thay thế tư vấn y tế.";
    }

    private String workProgressAnswer(User user) {
        if (user == null) {
            return "Bạn cần đăng nhập để xem tiến độ công việc.";
        }

        String role = user.getRole() != null ? user.getRole().name() : "USER";
        if ("ADMIN".equals(role)) {
            return buildTaskSummary("Toàn hệ thống", taskAssignmentRepository.findAllWithUser(), "/admin/tasks");
        }
        if ("MANAGER".equals(role)) {
            List<com.example.hr.models.TaskAssignment> all = taskAssignmentRepository.findAllWithUser();
            Integer deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
            List<com.example.hr.models.TaskAssignment> team = new ArrayList<>();
            for (com.example.hr.models.TaskAssignment a : all) {
                if (a.getUser() != null && a.getUser().getDepartment() != null
                        && Objects.equals(a.getUser().getDepartment().getId(), deptId)) {
                    team.add(a);
                }
            }
            return buildTaskSummary("Team của bạn", team, "/manager/team");
        }
        if ("HIRING".equals(role)) {
            return "Tiến độ tuyển dụng xem tại /hiring/dashboard, gồm pipeline ứng viên, lịch phỏng vấn và tin đăng tuyển dụng. Xem chi tiết ở Candidates hoặc Interviews.";
        }

        return buildTaskSummary("Công việc của bạn", taskAssignmentRepository.findByUser(user), "/user1/tasks");
    }

    private String buildTaskSummary(String scope, List<com.example.hr.models.TaskAssignment> rows, String link) {
        int total = rows != null ? rows.size() : 0;
        long pending = 0;
        long inProgress = 0;
        long completed = 0;
        long canceled = 0;

        if (rows != null) {
            for (com.example.hr.models.TaskAssignment a : rows) {
                String status = a.getStatus() != null ? a.getStatus().name() : "";
                if ("PENDING".equals(status)) pending++;
                else if ("IN_PROGRESS".equals(status)) inProgress++;
                else if ("COMPLETED".equals(status) || "DONE".equals(status)) completed++;
                else if ("CANCELED".equals(status)) canceled++;
            }
        }

        return scope + ": tổng " + total + " công việc. "
                + "Chưa bắt đầu: " + pending + ", đang làm: " + inProgress
                + ", hoàn thành: " + completed + ", đã hủy: " + canceled + ". "
                + "Xem chi tiết tại " + link + ".";
    }

    private String kpiResultAnswer(User user) {
        if (user == null) {
            return "Bạn cần đăng nhập để xem kết quả KPI.";
        }

        String role = user.getRole() != null ? user.getRole().name() : "USER";
        if ("ADMIN".equals(role)) {
            long active = kpiGoalRepository.findByStatus(com.example.hr.enums.KpiStatus.ACTIVE).size();
            long completed = kpiGoalRepository.findByStatus(com.example.hr.enums.KpiStatus.COMPLETED).size();
            return "KPI toàn hệ thống: đang thực hiện " + active + ", đã hoàn thành " + completed
                    + ". Xem chi tiết tại /admin/kpi.";
        }
        if ("MANAGER".equals(role) && user.getDepartment() != null) {
            List<com.example.hr.models.KpiGoal> goals = kpiGoalRepository.findByDepartmentId(user.getDepartment().getId());
            long active = goals.stream().filter(k -> k.getStatus() != null && "ACTIVE".equals(k.getStatus().name())).count();
            long completed = goals.stream().filter(k -> k.getStatus() != null && "COMPLETED".equals(k.getStatus().name())).count();
            return "KPI của team: tổng " + goals.size() + ", đang thực hiện " + active + ", đã hoàn thành " + completed
                    + ". Xem thêm ở /manager/performance.";
        }
        if ("HIRING".equals(role)) {
            return "Kết quả tuyển dụng xem tại /hiring/analytics/performance và /hiring/reports.";
        }

        List<com.example.hr.models.KpiGoal> activeGoals = kpiGoalRepository.findActiveGoalsByUser(user.getId(), LocalDate.now());
        Double avg = kpiGoalRepository.avgAchievementByUser(user.getId());
        return "KPI của bạn: đang active " + activeGoals.size()
                + ", điểm trung bình KPI đã hoàn thành: " + (avg != null ? String.format(Locale.ROOT, "%.1f%%", avg) : "chưa có")
                + ". Xem chi tiết tại /user1/kpi và /user1/reviews.";
    }

    private static boolean wantsEscalation(String norm, String raw) {
        return matches(norm, "gặp nhân sự", "chuyen hr", "chuyển hr", "lien he hr", "liên hệ hr", "escalate", "hotline hr")
                || raw.toLowerCase(Locale.ROOT).contains("hr ơi");
    }

    private static boolean matches(String norm, String... keys) {
        for (String k : keys) {
            if (norm.contains(normalize(k))) {
                return true;
            }
        }
        return false;
    }

    /** Chuẩn hóa nhẹ: thường + bỏ dấu tiếng Việt cơ bản để khớp từ khóa. */
    private static String normalize(String s) {
        String t = s.toLowerCase(Locale.ROOT).trim();
        t = t.replace('\u0111', 'd');
        t = java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return t;
    }

    // ==================== AI METHODS ====================

    /**
     * Gọi Gemini với context HR của user hiện tại.
     * Trả về null nếu AI không available hoặc lỗi.
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
     * Xây dựng system prompt với dữ liệu HR thực tế của user.
     */
    private String buildSystemPrompt(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý HR thông minh của hệ thống HRMS. ");
        sb.append("Trả lời ngắn gọn, chính xác bằng tiếng Việt. ");
        sb.append("Chỉ trả lời các câu hỏi liên quan đến HR, nhân sự, công việc. ");
        sb.append("Nếu câu hỏi không liên quan HR, từ chối lịch sự.\n\n");

        if (user != null) {
            sb.append("=== THÔNG TIN NHÂN VIÊN ===\n");
            sb.append("Tên: ").append(user.getFullName()).append("\n");
            sb.append("Email: ").append(user.getEmail() != null ? user.getEmail() : "N/A").append("\n");
            if (user.getDepartment() != null) {
                sb.append("Phòng ban: ").append(user.getDepartment().getDepartmentName()).append("\n");
            }
            if (user.getPosition() != null) {
                sb.append("Chức vụ: ").append(user.getPosition().getPositionName()).append("\n");
            }
            sb.append("Vai trò: ").append(user.getRole()).append("\n");

            // Leave info
            try {
                long pendingLeaves = leaveRequestRepository.findByUser(user).stream()
                        .filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
                long approvedLeaves = leaveRequestRepository.findByUser(user).stream()
                        .filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
                sb.append("\n=== NGHỈ PHÉP ===\n");
                sb.append("Đơn chờ duyệt: ").append(pendingLeaves).append("\n");
                sb.append("Đơn đã duyệt: ").append(approvedLeaves).append("\n");
            } catch (Exception ignored) {}

            // Payroll info
            try {
                int m = LocalDate.now().getMonthValue();
                int y = LocalDate.now().getYear();
                payrollRepository.findByUserIdAndMonthAndYear(user.getId(), m, y).ifPresent(p -> {
                    sb.append("\n=== LƯƠNG THÁNG ").append(m).append("/").append(y).append(" ===\n");
                    sb.append("Lương cơ bản: ").append(p.getBaseSalary()).append(" VND\n");
                    sb.append("Trạng thái: ").append(p.getPaymentStatus()).append("\n");
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
                sb.append("\n=== CÔNG VIỆC ===\n");
                sb.append("Chờ thực hiện: ").append(pendingTasks).append("\n");
                sb.append("Đang thực hiện: ").append(inProgressTasks).append("\n");
            } catch (Exception ignored) {}

            // KPI
            try {
                long activeKpi = kpiGoalRepository.findByUserId(user.getId()).stream()
                        .filter(k -> k.getStatus() != null && k.getStatus().name().equals("ACTIVE"))
                        .count();
                if (activeKpi > 0) {
                    sb.append("\n=== KPI ===\n");
                    sb.append("KPI đang active: ").append(activeKpi).append("\n");
                }
            } catch (Exception ignored) {}
        }

        sb.append("\n=== HƯỚNG DẪN ===\n");
        sb.append("- Nghỉ phép: /user/leaves\n");
        sb.append("- Phiếu lương: /user1/payroll\n");
        sb.append("- Chấm công: /user/attendance\n");
        sb.append("- Công việc: /user1/tasks\n");
        sb.append("- KPI: /user1/kpi\n");
        sb.append("- Thông báo: /notifications\n");
        sb.append("- Tài liệu: /user1/documents\n");
        sb.append("- Chi phí: /user1/expenses\n");

        return sb.toString();
    }
}
