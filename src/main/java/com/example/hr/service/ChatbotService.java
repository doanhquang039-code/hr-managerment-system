package com.example.hr.service;

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
        t = t.replace('đ', 'd');
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
