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
            flags.add("Thiáº¿u ngá»§");
            recommendations.add("Æ¯u tiÃªn ngá»§ 7-8 giá»; trÃ¡nh OT liÃªn tá»¥c náº¿u ngá»§ dÆ°á»›i 6 giá».");
        } else if (sleep < 7) {
            score -= 10;
            recommendations.add("Cá»‘ gáº¯ng tÄƒng thá»i lÆ°á»£ng ngá»§ thÃªm 30-60 phÃºt.");
        }

        if (stress >= 8) {
            score -= 24;
            flags.add("Stress cao");
            recommendations.add("Trao Ä‘á»•i vá»›i quáº£n lÃ½ hoáº·c HR náº¿u stress kÃ©o dÃ i; chia nhá» viá»‡c vÃ  Ä‘áº·t giá» nghá»‰ ngáº¯n.");
        } else if (stress >= 6) {
            score -= 12;
            recommendations.add("NÃªn nghá»‰ 5 phÃºt sau má»—i 60-90 phÃºt lÃ m viá»‡c táº­p trung.");
        }

        if (steps < 3000) {
            score -= 14;
            flags.add("Ãt váº­n Ä‘á»™ng");
            recommendations.add("Äi bá»™ ngáº¯n trong giá» nghá»‰; má»¥c tiÃªu tham kháº£o 5.000-7.000 bÆ°á»›c má»—i ngÃ y.");
        } else if (steps < 6000) {
            score -= 6;
            recommendations.add("TÄƒng váº­n Ä‘á»™ng nháº¹, vÃ­ dá»¥ Ä‘i cáº§u thang hoáº·c Ä‘i bá»™ sau bá»¯a trÆ°a.");
        }

        if (water < 1.2) {
            score -= 10;
            flags.add("Uá»‘ng Ã­t nÆ°á»›c");
            recommendations.add("Äáº·t nháº¯c uá»‘ng nÆ°á»›c; má»¥c tiÃªu tham kháº£o 1.5-2 lÃ­t má»—i ngÃ y náº¿u khÃ´ng cÃ³ chá»‘ng chá»‰ Ä‘á»‹nh y táº¿.");
        }

        if (overtime >= 4) {
            score -= 14;
            flags.add("OT cao");
            recommendations.add("Sau ngÃ y OT nhiá»u, nÃªn giáº£m táº£i ngÃ y káº¿ tiáº¿p hoáº·c sáº¯p xáº¿p láº¡i Æ°u tiÃªn cÃ´ng viá»‡c.");
        } else if (overtime >= 2) {
            score -= 6;
            recommendations.add("Theo dÃµi OT trong tuáº§n Ä‘á»ƒ trÃ¡nh tÃ­ch lÅ©y má»‡t má»i.");
        }

        String role = user != null && user.getRole() != null ? user.getRole().name() : "USER";
        if ("MANAGER".equals(role)) {
            recommendations.add("Vá»›i vai trÃ² quáº£n lÃ½, nÃªn kiá»ƒm tra táº£i viá»‡c cá»§a team náº¿u nhiá»u thÃ nh viÃªn cÃ¹ng stress cao.");
        } else if ("HIRING".equals(role)) {
            recommendations.add("Lá»‹ch phá»ng váº¥n dÃ y dá»… gÃ¢y má»‡t má»i; chá»«a khoáº£ng nghá»‰ giá»¯a cÃ¡c buá»•i phá»ng váº¥n.");
        } else if ("ADMIN".equals(role)) {
            recommendations.add("Admin nÃªn theo dÃµi xu hÆ°á»›ng stress vÃ  OT á»Ÿ cáº¥p há»‡ thá»‘ng thay vÃ¬ chá»‰ tá»«ng cÃ¡ nhÃ¢n.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("CÃ¡c chá»‰ sá»‘ Ä‘ang á»•n. Tiáº¿p tá»¥c duy trÃ¬ ngá»§ Ä‘á»§, váº­n Ä‘á»™ng nháº¹ vÃ  uá»‘ng nÆ°á»›c Ä‘á»u.");
        }

        score = Math.max(0, Math.min(100, score));
        String riskLevel = score >= 80 ? "LOW" : score >= 60 ? "MEDIUM" : "HIGH";
        String summary = switch (riskLevel) {
            case "HIGH" -> "CÃ³ dáº¥u hiá»‡u rá»§i ro sá»©c khá»e hoáº·c cÄƒng tháº³ng cao. NÃªn giáº£m táº£i vÃ  trao Ä‘á»•i vá»›i HR/quáº£n lÃ½ náº¿u tÃ¬nh tráº¡ng kÃ©o dÃ i.";
            case "MEDIUM" -> "CÃ³ vÃ i chá»‰ sá»‘ cáº§n chÃº Ã½. Äiá»u chá»‰nh ngá»§, váº­n Ä‘á»™ng, nÆ°á»›c uá»‘ng hoáº·c OT sáº½ cáº£i thiá»‡n Ä‘Ã¡ng ká»ƒ.";
            default -> "Chá»‰ sá»‘ sinh hoáº¡t Ä‘ang tÆ°Æ¡ng Ä‘á»‘i á»•n.";
        };

        return new HealthInsightResult(Math.round(score), riskLevel, summary, flags, recommendations,
                "ThÃ´ng tin chá»‰ mang tÃ­nh tham kháº£o, khÃ´ng thay tháº¿ tÆ° váº¥n y táº¿ chuyÃªn mÃ´n.");
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

