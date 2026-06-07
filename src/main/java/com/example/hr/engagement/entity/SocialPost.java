package com.example.hr.engagement.entity;

import com.example.hr.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String images; // JSON array of image URLs
    
    private String type = "POST"; // POST, ANNOUNCEMENT, ACHIEVEMENT
    
    @Column(nullable = false)
    private Boolean isPublic = true;
    
    private Integer likeCount = 0;
    private Integer commentCount = 0;
    
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


