package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exit_interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExitInterview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate interviewDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private User interviewer;
    
    @Column(columnDefinition = "TEXT")
    private String reasonForLeaving;
    
    private Integer satisfactionRating; // 1-5
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column(columnDefinition = "TEXT")
    private String suggestions;
    
    @Column(nullable = false)
    private Boolean wouldRecommend = false;
    
    @Column(nullable = false)
    private Boolean wouldRehire = false;
    
    @Column(columnDefinition = "TEXT")
    private String responses; // JSON cá»§a cÃ¡c cÃ¢u há»i/tráº£ lá»i
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


