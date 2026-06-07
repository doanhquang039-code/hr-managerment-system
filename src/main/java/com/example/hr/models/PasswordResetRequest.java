package com.example.hr.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_requests")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_identifier", nullable = false, length = 120)
    private String accountIdentifier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "request_source", nullable = false, length = 40)
    private String requestSource = "USERNAME";

    @Column(name = "requester_message", length = 500)
    private String requesterMessage;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}


