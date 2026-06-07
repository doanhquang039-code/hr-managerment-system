package com.example.hr.models; // 1. Äáº£m báº£o dÃ²ng nÃ y khá»›p vá»›i thÆ° má»¥c

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbotmessage")
@Data
public class ChatbotMessage { // 2. TÃªn class PHáº¢I khá»›p chÃ­nh xÃ¡c vá»›i tÃªn file
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "user_query", columnDefinition = "TEXT")
    private String userQuery;

    @Column(name = "bot_response", columnDefinition = "TEXT")
    private String botResponse;

    private String intent;
    private Integer rating;
    private Boolean isEscalated = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

