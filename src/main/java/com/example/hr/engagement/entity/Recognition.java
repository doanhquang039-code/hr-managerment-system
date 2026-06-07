package com.example.hr.engagement.entity;

import com.example.hr.models.User;
import jakarta.persistence.PreUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recognitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recognition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient; // NgÆ°á»i nháº­n
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_id", nullable = false)
    private User giver; // NgÆ°á»i táº·ng
    
    @Column(nullable = false)
    private String type; // THANK_YOU, GREAT_JOB, TEAM_PLAYER, INNOVATION, LEADERSHIP
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    private Integer points = 0; // Äiá»ƒm thÆ°á»Ÿng
    
    @Column(nullable = false)
    private Boolean isPublic = true;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


