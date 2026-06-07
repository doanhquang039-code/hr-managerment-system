package com.example.hr.models;


import com.example.hr.department.entity.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_type", length = 50)
    private String meetingType; // ONE_ON_ONE, TEAM_MEETING, REVIEW

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private User participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(length = 255)
    private String location;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(columnDefinition = "TEXT")
    private String agenda;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String actionItems;

    @Column(length = 50)
    private String status; // SCHEDULED, COMPLETED, CANCELLED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by_encrypted", length = 1000)
    private String createdByEncrypted;

    @Column(name = "updated_by_encrypted", length = 1000)
    private String updatedByEncrypted;
}


