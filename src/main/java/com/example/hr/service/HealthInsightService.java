package com.example.hr.service;

import com.example.hr.models.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HealthInsightService {

    public HealthInsightResult analyze(User user, HealthInsightInput input) {
        if (input == null) {
            input = new HealthInsightInput(null, null, null, null, null, null);
        }

        double score = 100;
        List<String> recommendations = new ArrayList<>();
        List<String> flags = new ArrayList<>();

        double sleep = input.sleepHours() != null ? input.sleepHours() : 7;
        int stress = input.stressLevel() != null ? input.stressLevel() : 3;
        int steps = input.steps() != null ? input.steps() : 6000;
        double water = input.waterLiters() != null ? input.waterLiters() : 1.8;
        double overtime = input.overtimeHours() != null ? input.overtimeHours() : 0;

        if (sleep < 6) {
            score -= 22;
            flags.add("Thiếu ngủ");
            recommendations.add("Ưu tiên ngủ 7-8 giờ; tránh OT liên tục nếu ngủ dưới 6 giờ.");
        } else if (sleep < 7) {
            score -= 10;
            recommendations.add("Cố gắng tăng thời lượng ngủ thêm 30-60 phút.");
        }

        if (stress >= 8) {
            score -= 24;
            flags.add("Stress cao");
            recommendations.add("Trao đổi với quản lý hoặc HR nếu stress kéo dài; chia nhỏ việc và đặt giờ nghỉ ngắn.");
        } else if (stress >= 6) {
            score -= 12;
            recommendations.add("Nên nghỉ 5 phút sau mỗi 60-90 phút làm việc tập trung.");
        }

        if (steps < 3000) {
            score -= 14;
            flags.add("Ít vận động");
            recommendations.add("Đi bộ ngắn trong giờ nghỉ; mục tiêu tham khảo 5.000-7.000 bước mỗi ngày.");
        } else if (steps < 6000) {
            score -= 6;
            recommendations.add("Tăng vận động nhẹ, ví dụ đi cầu thang hoặc đi bộ sau bữa trưa.");
        }

        if (water < 1.2) {
            score -= 10;
            flags.add("Uống ít nước");
            recommendations.add("Đặt nhắc uống nước; mục tiêu tham khảo 1.5-2 lít mỗi ngày nếu không có chống chỉ định y tế.");
        }

        if (overtime >= 4) {
            score -= 14;
            flags.add("OT cao");
            recommendations.add("Sau ngày OT nhiều, nên giảm tải ngày kế tiếp hoặc sắp xếp lại ưu tiên công việc.");
        } else if (overtime >= 2) {
            score -= 6;
            recommendations.add("Theo dõi OT trong tuần để tránh tích lũy mệt mỏi.");
        }

        String role = user != null && user.getRole() != null ? user.getRole().name() : "USER";
        if ("MANAGER".equals(role)) {
            recommendations.add("Với vai trò quản lý, nên kiểm tra tải việc của team nếu nhiều thành viên cùng stress cao.");
        } else if ("HIRING".equals(role)) {
            recommendations.add("Lịch phỏng vấn dày dễ gây mệt mỏi; chừa khoảng nghỉ giữa các buổi phỏng vấn.");
        } else if ("ADMIN".equals(role)) {
            recommendations.add("Admin nên theo dõi xu hướng stress và OT ở cấp hệ thống thay vì chỉ từng cá nhân.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Các chỉ số đang ổn. Tiếp tục duy trì ngủ đủ, vận động nhẹ và uống nước đều.");
        }

        score = Math.max(0, Math.min(100, score));
        String riskLevel = score >= 80 ? "LOW" : score >= 60 ? "MEDIUM" : "HIGH";
        String summary = switch (riskLevel) {
            case "HIGH" -> "Có dấu hiệu rủi ro sức khỏe hoặc căng thẳng cao. Nên giảm tải và trao đổi với HR/quản lý nếu tình trạng kéo dài.";
            case "MEDIUM" -> "Có vài chỉ số cần chú ý. Điều chỉnh ngủ, vận động, nước uống hoặc OT sẽ cải thiện đáng kể.";
            default -> "Chỉ số sinh hoạt đang tương đối ổn.";
        };

        return new HealthInsightResult(Math.round(score), riskLevel, summary, flags, recommendations,
                "Thông tin chỉ mang tính tham khảo, không thay thế tư vấn y tế chuyên môn.");
    }

    public record HealthInsightInput(
            Double sleepHours,
            Integer stressLevel,
            Integer steps,
            Double waterLiters,
            Double overtimeHours,
            String mood
    ) {}

    public record HealthInsightResult(
            long wellnessScore,
            String riskLevel,
            String summary,
            List<String> flags,
            List<String> recommendations,
            String disclaimer
    ) {}
}