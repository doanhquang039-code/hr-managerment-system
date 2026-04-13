package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "candidate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;

    @Column(name = "applied_date")
    private LocalDate appliedDate = LocalDate.now();

    @Column(length = 30)
    private String status = "NEW"; // NEW, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Helper: get status badge color
    public String getStatusColor() {
        return switch (status) {
            case "NEW" -> "secondary";
            case "SCREENING" -> "info";
            case "INTERVIEW" -> "primary";
            case "OFFER" -> "warning";
            case "HIRED" -> "success";
            case "REJECTED" -> "danger";
            default -> "light";
        };
    }

    public String getStatusLabel() {
        return switch (status) {
            case "NEW" -> "Mới";
            case "SCREENING" -> "Sàng lọc";
            case "INTERVIEW" -> "Phỏng vấn";
            case "OFFER" -> "Offer";
            case "HIRED" -> "Đã tuyển";
            case "REJECTED" -> "Từ chối";
            default -> status;
        };
    }
}
