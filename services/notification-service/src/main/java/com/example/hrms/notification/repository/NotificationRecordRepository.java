package com.example.hrms.notification.repository;

import com.example.hrms.notification.domain.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {
    List<NotificationRecord> findByRecipientUserIdOrderByCreatedAtDesc(Integer recipientUserId);
    long countByRecipientUserIdAndReadFlagFalse(Integer recipientUserId);
}
