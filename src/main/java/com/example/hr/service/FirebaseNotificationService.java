package com.example.hr.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging (FCM) — push notifications realtime.
 * Chỉ active khi firebase.enabled=true.
 *
 * Flow:
 * 1. Frontend đăng ký FCM token khi user login
 * 2. Backend lưu token vào DB (user.fcmToken)
 * 3. Khi có event → gọi service này để push notification
 */
@ConditionalOnBean(FirebaseApp.class)
public class FirebaseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseNotificationService.class);

    // ==================== SINGLE DEVICE ====================

    /**
     * Gửi push notification đến một thiết bị cụ thể.
     */
    public String sendToDevice(String fcmToken, String title, String body,
                                Map<String, String> data) {
        try {
            Message.Builder builder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setSound("default").build())
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("FCM sent to device: {}", response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed: {}", e.getMessage());
            return null;
        }
    }

    // ==================== TOPIC (broadcast) ====================

    /**
     * Gửi notification đến một topic (nhóm users đã subscribe).
     * VD: topic "dept-IT" cho toàn bộ phòng IT.
     */
    public String sendToTopic(String topic, String title, String body,
                               Map<String, String> data) {
        try {
            Message.Builder builder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("FCM sent to topic '{}': {}", topic, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM topic send failed: {}", e.getMessage());
            return null;
        }
    }

    // ==================== MULTICAST (nhiều thiết bị) ====================

    /**
     * Gửi notification đến nhiều FCM tokens cùng lúc (tối đa 500).
     */
    public BatchResponse sendToMultiple(List<String> tokens, String title,
                                         String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) return null;

        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendEachForMulticast(builder.build());
            log.info("FCM multicast: {}/{} success", response.getSuccessCount(), tokens.size());
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed: {}", e.getMessage());
            return null;
        }
    }

    // ==================== SUBSCRIBE / UNSUBSCRIBE TOPIC ====================

    /**
     * Subscribe danh sách tokens vào một topic.
     */
    public void subscribeToTopic(List<String> tokens, String topic) {
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .subscribeToTopic(tokens, topic);
            log.info("Subscribed {} tokens to topic '{}', {} errors",
                    response.getSuccessCount(), topic, response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Subscribe to topic failed: {}", e.getMessage());
        }
    }

    /**
     * Unsubscribe tokens khỏi topic.
     */
    public void unsubscribeFromTopic(List<String> tokens, String topic) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(tokens, topic);
            log.info("Unsubscribed tokens from topic '{}'", topic);
        } catch (FirebaseMessagingException e) {
            log.error("Unsubscribe from topic failed: {}", e.getMessage());
        }
    }

    // ==================== HRMS SPECIFIC HELPERS ====================

    /** Thông báo payslip mới */
    public void notifyPayslip(String fcmToken, String employeeName, int month, int year) {
        sendToDevice(fcmToken,
                "💰 Phiếu lương tháng " + month + "/" + year,
                "Phiếu lương của " + employeeName + " đã sẵn sàng. Nhấn để xem.",
                Map.of("type", "PAYROLL", "url", "/user1/payroll"));
    }

    /** Thông báo đơn nghỉ phép được duyệt */
    public void notifyLeaveApproved(String fcmToken, String leaveType, String date) {
        sendToDevice(fcmToken,
                "✅ Đơn nghỉ phép được duyệt",
                "Đơn " + leaveType + " ngày " + date + " đã được phê duyệt.",
                Map.of("type", "LEAVE", "url", "/user/leaves"));
    }

    /** Thông báo KPI mới */
    public void notifyKpiAssigned(String fcmToken, String goalTitle) {
        sendToDevice(fcmToken,
                "🎯 KPI Goal mới",
                "Bạn được giao mục tiêu: " + goalTitle,
                Map.of("type", "KPI", "url", "/user1/kpi"));
    }

    /** Broadcast thông báo công ty đến tất cả nhân viên */
    public void broadcastAnnouncement(String title, String content) {
        sendToTopic("all-employees", "📢 " + title, content,
                Map.of("type", "ANNOUNCEMENT", "url", "/user1/announcements"));
    }

    /** Thông báo đến phòng ban cụ thể */
    public void notifyDepartment(String deptCode, String title, String body) {
        sendToTopic("dept-" + deptCode.toLowerCase(), title, body,
                Map.of("type", "DEPT_NOTICE"));
    }
}
