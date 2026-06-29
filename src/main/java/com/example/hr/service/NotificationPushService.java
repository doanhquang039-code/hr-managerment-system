package com.example.hr.service;

import com.example.hr.models.Notification;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.websocket.NotificationWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationPushService – Đẩy notification real-time qua WebSocket STOMP.
 * Khi admin/manager tạo notification mới, service này sẽ broadcast
 * đến tất cả user đang online có role tương ứng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPushService {

    private final NotificationWebSocketController webSocketController;
    private final UserRepository userRepository;

    /**
     * Đẩy notification đến một user cụ thể qua WebSocket.
     * Dùng sau khi đã lưu Notification vào DB.
     */
    public void pushToUser(User user, Notification notification) {
        try {
            Map<String, Object> payload = buildPayload(notification);
            webSocketController.sendToUser(user.getUsername(), payload);
            log.debug("Pushed notification to user '{}': {}", user.getUsername(), notification.getMessage());
        } catch (Exception e) {
            log.warn("Failed to push WebSocket notification to user '{}': {}", user.getUsername(), e.getMessage());
        }
    }

    /**
     * Broadcast notification đến tất cả user có role HIRING và USER.
     * Dùng khi admin/manager tạo thông báo chung cho nhân viên.
     */
    public void pushToHiringAndUsers(Notification templateNotif) {
        try {
            List<User> allUsers = userRepository.findAll();
            Map<String, Object> payload = buildPayload(templateNotif);

            for (User u : allUsers) {
                String roleName = u.getEffectiveRoleName();
                if ("HIRING".equalsIgnoreCase(roleName) || "USER".equalsIgnoreCase(roleName)) {
                    try {
                        webSocketController.sendToUser(u.getUsername(), new HashMap<>(payload));
                    } catch (Exception ex) {
                        log.warn("Failed push to user '{}': {}", u.getUsername(), ex.getMessage());
                    }
                }
            }
            log.info("Broadcast notification to HIRING+USER roles: {}", templateNotif.getMessage());
        } catch (Exception e) {
            log.error("Error broadcasting notification to HIRING+USER: {}", e.getMessage());
        }
    }

    /**
     * Broadcast notification đến tất cả user có role HIRING và ADMIN.
     * Dùng khi có CV mới từ trang tuyển dụng công khai.
     */
    public void pushToHiringAndAdmins(String message, String link) {
        try {
            List<User> allUsers = userRepository.findAll();
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("type", "NEW_APPLICATION");
            payload.put("link", link);

            for (User u : allUsers) {
                String roleName = u.getEffectiveRoleName();
                if ("HIRING".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
                    try {
                        webSocketController.sendToUser(u.getUsername(), new HashMap<>(payload));
                    } catch (Exception ex) {
                        log.warn("Failed push to user '{}': {}", u.getUsername(), ex.getMessage());
                    }
                }
            }
            log.info("Broadcast new-application notification to HIRING+ADMIN: {}", message);
        } catch (Exception e) {
            log.error("Error broadcasting new-application notification: {}", e.getMessage());
        }
    }

    /**
     * Broadcast notification đến tất cả role được chỉ định.
     */
    public void pushToRoles(Notification notification, List<String> roles) {
        try {
            List<User> allUsers = userRepository.findAll();
            Map<String, Object> payload = buildPayload(notification);

            for (User u : allUsers) {
                String roleName = u.getEffectiveRoleName();
                boolean matches = roles.stream().anyMatch(r -> r.equalsIgnoreCase(roleName));
                if (matches) {
                    try {
                        webSocketController.sendToUser(u.getUsername(), new HashMap<>(payload));
                    } catch (Exception ex) {
                        log.warn("Failed push to user '{}': {}", u.getUsername(), ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting notification to roles {}: {}", roles, e.getMessage());
        }
    }

    // ---- helpers ----

    private Map<String, Object> buildPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notification.getId());
        payload.put("message", notification.getMessage());
        payload.put("type", notification.getType() != null ? notification.getType().name() : "INFO");
        payload.put("link", notification.getLink());
        payload.put("read", notification.isRead());
        return payload;
    }
}
