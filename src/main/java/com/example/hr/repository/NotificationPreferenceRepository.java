package com.example.hr.repository;

import com.example.hr.enums.NotificationChannel;
import com.example.hr.models.NotificationPreference;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    List<NotificationPreference> findByUser(User user);
    
    Optional<NotificationPreference> findByUserAndNotificationTypeAndChannel(
        User user, String notificationType, NotificationChannel channel);
    
    List<NotificationPreference> findByUserAndEnabled(User user, Boolean enabled);
}


