package com.example.hr.minigame.service;

import com.example.hr.enums.Role;
import com.example.hr.models.Quiz;
import com.example.hr.models.QuizAttempt;
import com.example.hr.models.QuizQuestion;
import com.example.hr.models.User;
import com.example.hr.repository.QuizAttemptRepository;
import com.example.hr.repository.QuizQuestionRepository;
import com.example.hr.repository.QuizRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MinigameService {

    private static final String DEFAULT_TITLE = "HR Quick Challenge";
    private static final String ARCADE_TITLE = "HR Arcade Run";

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MinigameView getGame(Integer quizId, User user) {
        getOrCreateDefaultQuiz();
        List<Quiz> games = quizRepository.findByCourseIsNullAndLessonIsNullAndIsActiveTrueOrderByCreatedAtDesc();
        Quiz quiz = resolveQuiz(quizId, games);
        List<QuizQuestion> questions = questionRepository.findByQuizOrderByOrderIndexAsc(quiz);
        List<MinigameQuestion> questionCards = questions.stream()
                .map(this::toQuestionCard)
                .toList();
        QuizAttempt bestAttempt = user == null ? null
                : attemptRepository.findFirstByUserAndQuizOrderByScoreDesc(user, quiz).orElse(null);
        List<QuizAttempt> leaderboard = attemptRepository.findLeaderboardByQuiz(quiz).stream()
                .limit(10)
                .toList();
        return new MinigameView(games, quiz, questionCards, bestAttempt, leaderboard);
    }

    @Transactional
    public QuizAttempt submit(Integer quizId, User user, Map<Integer, String> answers) {
        if (user == null) {
            throw new IllegalStateException("Khong tim thay nguoi dung hien tai");
        }

        Quiz quiz = quizRepository.findById(quizId).orElseGet(this::getOrCreateDefaultQuiz);
        List<QuizQuestion> questions = questionRepository.findByQuizOrderByOrderIndexAsc(quiz);
        int score = 0;
        int totalPoints = 0;
        Map<Integer, String> normalizedAnswers = new LinkedHashMap<>();

        for (QuizQuestion question : questions) {
            int points = question.getPoints() == null ? 1 : question.getPoints();
            totalPoints += points;
            String submitted = answers.get(question.getId());
            if (submitted != null) {
                submitted = submitted.trim();
                normalizedAnswers.put(question.getId(), submitted);
            }
            if (submitted != null && submitted.equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                score += points;
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(score);
        attempt.setTotalPoints(totalPoints);
        attempt.setPassed(totalPoints == 0 || score * 100 >= totalPoints * quiz.getPassingScore());
        attempt.setAnswers(writeAnswers(normalizedAnswers));
        attempt.setCompletedAt(LocalDateTime.now());
        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitArcadeScore(User user, Integer score) {
        if (user == null) {
            throw new IllegalStateException("Khong tim thay nguoi dung hien tai");
        }
        Quiz quiz = quizRepository.findFirstByTitleAndCourseIsNullAndLessonIsNull(ARCADE_TITLE)
                .orElseGet(this::createArcadeQuiz);
        int safeScore = Math.max(0, Math.min(score == null ? 0 : score, 100));
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(safeScore);
        attempt.setTotalPoints(100);
        attempt.setPassed(safeScore >= 50);
        attempt.setAnswers("{\"mode\":\"arcade\"}");
        attempt.setCompletedAt(LocalDateTime.now());
        return attemptRepository.save(attempt);
    }

    @Transactional
    public Quiz createGame(String title,
                           String description,
                           Integer timeLimit,
                           Integer passingScore,
                           String rewardTop1,
                           String rewardTop2,
                           String rewardTop3,
                           List<String> questionTexts,
                           List<String> optionA,
                           List<String> optionB,
                           List<String> optionC,
                           List<String> optionD,
                           List<String> correctAnswers) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setTimeLimit(timeLimit == null ? 5 : timeLimit);
        quiz.setPassingScore(passingScore == null ? 60 : passingScore);
        quiz.setRewardTop1(rewardTop1);
        quiz.setRewardTop2(rewardTop2);
        quiz.setRewardTop3(rewardTop3);
        quiz.setIsActive(true);
        Quiz saved = quizRepository.save(quiz);

        int order = 1;
        for (int i = 0; i < questionTexts.size(); i++) {
            String text = questionTexts.get(i);
            if (text == null || text.isBlank()) {
                continue;
            }
            List<String> options = List.of(valueAt(optionA, i), valueAt(optionB, i), valueAt(optionC, i), valueAt(optionD, i));
            String answer = valueAt(correctAnswers, i);
            if (answer == null || answer.isBlank()) {
                answer = options.get(0);
            }
            QuizQuestion question = question(saved, order++, text, options, answer, "Cau tra loi dung: " + answer);
            questionRepository.save(question);
        }

        if (questionRepository.countByQuiz(saved) == 0) {
            seedDefaultQuestions(saved);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public AdminLeaderboardView getLeaderboard(Integer quizId, Role role) {
        getOrCreateDefaultQuiz();
        List<Quiz> games = quizRepository.findByCourseIsNullAndLessonIsNullAndIsActiveTrueOrderByCreatedAtDesc();
        Quiz quiz = resolveQuiz(quizId, games);
        List<QuizAttempt> attempts = (role == null
                ? attemptRepository.findLeaderboardByQuiz(quiz)
                : attemptRepository.findLeaderboardByQuizAndRole(quiz, role)).stream()
                .limit(50)
                .toList();
        return new AdminLeaderboardView(games, quiz, role, attempts);
    }

    private Quiz resolveQuiz(Integer quizId, List<Quiz> games) {
        if (quizId != null) {
            Optional<Quiz> selected = games.stream()
                    .filter(game -> game.getId().equals(quizId))
                    .findFirst();
            if (selected.isPresent()) {
                return selected.get();
            }
        }
        if (!games.isEmpty()) {
            return games.get(0);
        }
        return getOrCreateDefaultQuiz();
    }

    private Quiz getOrCreateDefaultQuiz() {
        Quiz quiz = quizRepository.findFirstByTitleAndCourseIsNullAndLessonIsNull(DEFAULT_TITLE)
                .orElseGet(this::createDefaultQuiz);
        if (questionRepository.countByQuiz(quiz) == 0) {
            seedDefaultQuestions(quiz);
        }
        return quiz;
    }

    private Quiz createDefaultQuiz() {
        Quiz quiz = new Quiz();
        quiz.setTitle(DEFAULT_TITLE);
        quiz.setDescription("Thu thach nhanh ve HR, van hoa cong ty va ky nang noi bo.");
        quiz.setTimeLimit(5);
        quiz.setPassingScore(60);
        quiz.setRewardTop1("Voucher cafe 100k");
        quiz.setRewardTop2("Voucher an trua 70k");
        quiz.setRewardTop3("Huy hieu HRMS");
        quiz.setIsActive(true);
        return quizRepository.save(quiz);
    }

    private Quiz createArcadeQuiz() {
        Quiz quiz = new Quiz();
        quiz.setTitle(ARCADE_TITLE);
        quiz.setDescription("Game 2D bat qua HR, tranh vat can va dua diem len bang xep hang.");
        quiz.setTimeLimit(1);
        quiz.setPassingScore(50);
        quiz.setRewardTop1("Voucher giai tri 200k");
        quiz.setRewardTop2("Voucher cafe 100k");
        quiz.setRewardTop3("Huy hieu Arcade");
        quiz.setIsActive(true);
        return quizRepository.save(quiz);
    }

    private void seedDefaultQuestions(Quiz quiz) {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(question(quiz, 1,
                "Khi can nghi phep, buoc nao nen lam dau tien?",
                List.of("Bao mieng voi dong nghiep", "Tao don nghi phep tren he thong", "Tu nghi roi bo sung sau", "Gui tin nhan rieng cho admin"),
                "Tao don nghi phep tren he thong",
                "Don tren he thong giup quan ly phe duyet va cham cong ro rang."));
        questions.add(question(quiz, 2,
                "Du lieu nao can duoc bao mat cao trong he thong HR?",
                List.of("So dien thoai ca nhan", "Bang luong", "CCCD/CMND", "Tat ca cac thong tin tren"),
                "Tat ca cac thong tin tren",
                "Thong tin nhan su va tai chinh deu la du lieu nhay cam."));
        questions.add(question(quiz, 3,
                "Neu quen check-in, cach xu ly phu hop la gi?",
                List.of("Bo qua vi khong quan trong", "Tu sua truc tiep database", "Bao quan ly/HR va tao yeu cau dieu chinh", "Check-in bu vao ngay hom sau"),
                "Bao quan ly/HR va tao yeu cau dieu chinh",
                "Can co xac nhan cua quan ly/HR de du lieu cham cong minh bach."));
        questions.add(question(quiz, 4,
                "KPI tot nen co dac diem nao?",
                List.of("Ro rang va do luong duoc", "Cang dai cang tot", "Chi can giao bang loi noi", "Khong can thoi han"),
                "Ro rang va do luong duoc",
                "KPI can cu the, do luong duoc va co moc thoi gian."));
        questions.add(question(quiz, 5,
                "Vinh danh dong nghiep nen tap trung vao dieu gi?",
                List.of("Ket qua va hanh vi cu the", "Noi chung chung cho nhanh", "Chi danh cho quan ly", "Chi khi co thuong tien"),
                "Ket qua va hanh vi cu the",
                "Ghi nhan cu the giup loi khen co y nghia va de lan toa."));
        questionRepository.saveAll(questions);
    }

    private QuizQuestion question(Quiz quiz, int order, String text, List<String> options, String answer, String explanation) {
        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setQuestion(text);
        question.setType("MULTIPLE_CHOICE");
        question.setOptions(writeOptions(options));
        question.setCorrectAnswer(answer);
        question.setPoints(10);
        question.setOrderIndex(order);
        question.setExplanation(explanation);
        return question;
    }

    private MinigameQuestion toQuestionCard(QuizQuestion question) {
        return new MinigameQuestion(question, readOptions(question.getOptions()));
    }

    private String writeOptions(List<String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String writeAnswers(Map<Integer, String> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String valueAt(List<String> values, int index) {
        if (values == null || index >= values.size()) {
            return "";
        }
        String value = values.get(index);
        return value == null ? "" : value.trim();
    }

    private List<String> readOptions(String options) {
        if (options == null || options.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(options, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    public record MinigameQuestion(QuizQuestion question, List<String> options) {
    }

    public record MinigameView(
            List<Quiz> games,
            Quiz quiz,
            List<MinigameQuestion> questions,
            QuizAttempt bestAttempt,
            List<QuizAttempt> leaderboard
    ) {
    }

    public record AdminLeaderboardView(
            List<Quiz> games,
            Quiz quiz,
            Role selectedRole,
            List<QuizAttempt> leaderboard
    ) {
    }
}
