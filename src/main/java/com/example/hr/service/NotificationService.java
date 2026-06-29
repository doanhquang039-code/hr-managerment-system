package com.example.hr.service;

import com.example.hr.enums.NotificationType;
import com.example.hr.models.Notification;
import com.example.hr.models.User;
import com.example.hr.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Lazy để tránh circular dependency (NotificationPushService -> UserRepository -> ...) */
    @Autowired @Lazy
    private NotificationPushService notificationPushService;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User user, String message, NotificationType type, String link) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setMessage(message);
        notif.setType(type);
        notif.setLink(link);
        notif.setRead(false);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);
        // Push real-time WebSocket notification ngay sau khi lưu DB
        try {
            notificationPushService.pushToUser(user, notif);
        } catch (Exception e) {
            // WebSocket push failure không được làm hỏng flow chính
        }
    }

    public void notify(User user, String message) {
        createNotification(user, message, NotificationType.INFO, null);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAll(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnread(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getRecent(User user, int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 50));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, boundedLimit));
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAllRead(User user) {
        notificationRepository.markAllReadByUser(user);
    }

    public boolean markRead(Integer id, User user) {
        return notificationRepository.markReadByIdAndUser(id, user) > 0;
    }
}


