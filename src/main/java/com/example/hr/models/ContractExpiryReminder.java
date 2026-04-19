package com.example.hr.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contract_expiry_reminder",
        uniqueConstraints = @UniqueConstraint(name = "uk_contract_reminder", columnNames = {"contract_id", "reminder_days"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractExpiryReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "reminder_days", nullable = false)
    private Integer reminderDays;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
