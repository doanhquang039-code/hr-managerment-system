package com.example.hr.repository;

import com.example.hr.enums.NotificationChannel;
import com.example.hr.models.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByCodeAndChannel(String code, NotificationChannel channel);
    
    List<NotificationTemplate> findByChannelAndIsActive(NotificationChannel channel, Boolean isActive);
    
    List<NotificationTemplate> findByIsActiveOrderByNameAsc(Boolean isActive);
}


