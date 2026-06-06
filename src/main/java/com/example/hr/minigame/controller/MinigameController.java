package com.example.hr.minigame.controller;

import com.example.hr.enums.Role;
import com.example.hr.models.QuizAttempt;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.minigame.service.MinigameService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MinigameController {

    private final MinigameService minigameService;
    private final AuthUserHelper authUserHelper;

    @GetMapping({"/minigame", "/user1/minigame", "/hiring/minigame", "/manager/minigame", "/admin/minigame"})
    public String index(@RequestParam(required = false) Integer quizId, Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        var game = minigameService.getGame(quizId, user);
        model.addAttribute("user", user);
        model.addAttribute("currentRole", user != null && user.getRole() != null ? user.getRole().name() : "USER");
        model.addAttribute("games", game.games());
        model.addAttribute("quiz", game.quiz());
        model.addAttribute("questions", game.questions());
        model.addAttribute("bestAttempt", game.bestAttempt());
        model.addAttribute("leaderboard", game.leaderboard());
        return "user1/minigame";
    }

    @PostMapping({"/minigame/submit", "/user1/minigame/submit", "/hiring/minigame/submit", "/manager/minigame/submit", "/admin/minigame/submit"})
    public String submit(@RequestParam Integer quizId,
                         @RequestParam Map<String, String> params,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            User user = authUserHelper.getCurrentUser(auth);
            Map<Integer, String> answers = params.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("answer_"))
                    .collect(java.util.stream.Collectors.toMap(
                            entry -> Integer.parseInt(entry.getKey().substring("answer_".length())),
                            Map.Entry::getValue
            ));
            QuizAttempt attempt = minigameService.submit(quizId, user, answers);
            ra.addFlashAttribute("success", "Ban dat " + attempt.getScore() + "/" + attempt.getTotalPoints() + " diem!");
            return "redirect:" + minigamePathFor(user) + "?quizId=" + quizId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the nop bai minigame: " + e.getMessage());
        }
        return "redirect:/user1/minigame";
    }

    @PostMapping({"/minigame/arcade-score", "/user1/minigame/arcade-score", "/hiring/minigame/arcade-score", "/manager/minigame/arcade-score", "/admin/minigame/arcade-score"})
    public String arcadeScore(@RequestParam Integer score,
                              Authentication auth,
                              RedirectAttributes ra) {
        return saveArcadeScore(score, auth, ra);
    }

    @GetMapping({"/minigame/arcade-score", "/user1/minigame/arcade-score", "/hiring/minigame/arcade-score", "/manager/minigame/arcade-score", "/admin/minigame/arcade-score"})
    public String arcadeScoreGet(@RequestParam Integer score,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        return saveArcadeScore(score, auth, ra);
    }

    private String saveArcadeScore(Integer score, Authentication auth, RedirectAttributes ra) {
        User user = authUserHelper.getCurrentUser(auth);
        try {
            QuizAttempt attempt = minigameService.submitArcadeScore(user, score);
            ra.addFlashAttribute("success", "Arcade score: " + attempt.getScore() + "/100 diem!");
            return "redirect:" + minigamePathFor(user);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the luu diem arcade: " + e.getMessage());
            return "redirect:/user1/minigame";
        }
    }

    @GetMapping("/manager/minigames/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("questionSlots", List.of(1, 2, 3, 4, 5));
        return "manager/minigame-form";
    }

    @PostMapping("/manager/minigames/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) Integer timeLimit,
                         @RequestParam(required = false) Integer passingScore,
                         @RequestParam(required = false) String rewardTop1,
                         @RequestParam(required = false) String rewardTop2,
                         @RequestParam(required = false) String rewardTop3,
                         @RequestParam List<String> questionText,
                         @RequestParam List<String> optionA,
                         @RequestParam List<String> optionB,
                         @RequestParam List<String> optionC,
                         @RequestParam List<String> optionD,
                         @RequestParam List<String> correctAnswer,
                         RedirectAttributes ra) {
        try {
            var quiz = minigameService.createGame(title, description, timeLimit, passingScore,
                    rewardTop1, rewardTop2, rewardTop3, questionText, optionA, optionB, optionC, optionD, correctAnswer);
            ra.addFlashAttribute("success", "Da tao minigame: " + quiz.getTitle());
            return "redirect:/manager/minigame?quizId=" + quiz.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Khong the tao minigame: " + e.getMessage());
            return "redirect:/manager/minigames/create";
        }
    }

    @GetMapping("/admin/minigame/top")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTop(@RequestParam(required = false) Integer quizId,
                           @RequestParam(required = false) Role role,
                           Model model) {
        var view = minigameService.getLeaderboard(quizId, role);
        model.addAttribute("games", view.games());
        model.addAttribute("quiz", view.quiz());
        model.addAttribute("selectedRole", view.selectedRole());
        model.addAttribute("roles", Role.values());
        model.addAttribute("leaderboard", view.leaderboard());
        return "admin/minigame-top";
    }

    private String minigamePathFor(User user) {
        if (user == null || user.getRole() == null) {
            return "/user1/minigame";
        }
        return switch (user.getRole()) {
            case ADMIN -> "/admin/minigame";
            case MANAGER -> "/manager/minigame";
            case HIRING -> "/hiring/minigame";
            case USER -> "/user1/minigame";
        };
    }
}
