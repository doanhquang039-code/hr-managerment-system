package com.example.hr.service;


import com.example.hr.payroll.entity.Payroll;
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
 * Firebase Cloud Messaging (FCM) â€” push notifications realtime.
 * Chá»‰ active khi firebase.enabled=true.
 *
 * Flow:
 * 1. Frontend Ä‘Äƒng kÃ½ FCM token khi user login
 * 2. Backend lÆ°u token vÃ o DB (user.fcmToken)
 * 3. Khi cÃ³ event â†’ gá»i service nÃ y Ä‘á»ƒ push notification
 */
@ConditionalOnBean(FirebaseApp.class)
public class FirebaseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseNotificationService.class);

    // ==================== SINGLE DEVICE ====================

    /**
     * Gá»­i push notification Ä‘áº¿n má»™t thiáº¿t bá»‹ cá»¥ thá»ƒ.
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
     * Gá»­i notification Ä‘áº¿n má»™t topic (nhÃ³m users Ä‘Ã£ subscribe).
     * VD: topic "dept-IT" cho toÃ n bá»™ phÃ²ng IT.
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

    // ==================== MULTICAST (nhiá»u thiáº¿t bá»‹) ====================

    /**
     * Gá»­i notification Ä‘áº¿n nhiá»u FCM tokens cÃ¹ng lÃºc (tá»‘i Ä‘a 500).
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
     * Subscribe danh sÃ¡ch tokens vÃ o má»™t topic.
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
     * Unsubscribe tokens khá»i topic.
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

    /** ThÃ´ng bÃ¡o payslip má»›i */
    public void notifyPayslip(String fcmToken, String employeeName, int month, int year) {
        sendToDevice(fcmToken,
                "ðŸ’° Phiáº¿u lÆ°Æ¡ng thÃ¡ng " + month + "/" + year,
                "Phiáº¿u lÆ°Æ¡ng cá»§a " + employeeName + " Ä‘Ã£ sáºµn sÃ ng. Nháº¥n Ä‘á»ƒ xem.",
                Map.of("type", "PAYROLL", "url", "/user1/payroll"));
    }

    /** ThÃ´ng bÃ¡o Ä‘Æ¡n nghá»‰ phÃ©p Ä‘Æ°á»£c duyá»‡t */
    public void notifyLeaveApproved(String fcmToken, String leaveType, String date) {
        sendToDevice(fcmToken,
                "âœ… ÄÆ¡n nghá»‰ phÃ©p Ä‘Æ°á»£c duyá»‡t",
                "ÄÆ¡n " + leaveType + " ngÃ y " + date + " Ä‘Ã£ Ä‘Æ°á»£c phÃª duyá»‡t.",
                Map.of("type", "LEAVE", "url", "/user/leaves"));
    }

    /** ThÃ´ng bÃ¡o KPI má»›i */
    public void notifyKpiAssigned(String fcmToken, String goalTitle) {
        sendToDevice(fcmToken,
                "ðŸŽ¯ KPI Goal má»›i",
                "Báº¡n Ä‘Æ°á»£c giao má»¥c tiÃªu: " + goalTitle,
                Map.of("type", "KPI", "url", "/user1/kpi"));
    }

    /** Broadcast thÃ´ng bÃ¡o cÃ´ng ty Ä‘áº¿n táº¥t cáº£ nhÃ¢n viÃªn */
    public void broadcastAnnouncement(String title, String content) {
        sendToTopic("all-employees", "ðŸ“¢ " + title, content,
                Map.of("type", "ANNOUNCEMENT", "url", "/user1/announcements"));
    }

    /** ThÃ´ng bÃ¡o Ä‘áº¿n phÃ²ng ban cá»¥ thá»ƒ */
    public void notifyDepartment(String deptCode, String title, String body) {
        sendToTopic("dept-" + deptCode.toLowerCase(), title, body,
                Map.of("type", "DEPT_NOTICE"));
    }
}


