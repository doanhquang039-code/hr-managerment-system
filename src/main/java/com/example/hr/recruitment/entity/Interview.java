package com.example.hr.recruitment.entity;

import com.example.hr.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id")
    private User interviewer;

    @Column(name = "interview_type", length = 50)
    private String interviewType; // PHONE, VIDEO, IN_PERSON, TECHNICAL, HR

    @Column(name = "interview_round")
    private Integer interviewRound = 1;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @Column(length = 255)
    private String location;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(columnDefinition = "TEXT")
    private String agenda;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "technical_score")
    private Integer technicalScore; // 1-10

    @Column(name = "communication_score")
    private Integer communicationScore; // 1-10

    @Column(name = "cultural_fit_score")
    private Integer culturalFitScore; // 1-10

    @Column(name = "overall_score")
    private Integer overallScore; // 1-100

    @Column(length = 50)
    private String recommendation; // STRONG_HIRE, HIRE, NO_HIRE, STRONG_NO_HIRE

    @Column(length = 50)
    private String status; // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void calculateOverallScore() {
        if (technicalScore != null && communicationScore != null && culturalFitScore != null) {
            this.overallScore = (int) ((technicalScore + communicationScore + culturalFitScore) * 100.0 / 30);
        }
    }
}

