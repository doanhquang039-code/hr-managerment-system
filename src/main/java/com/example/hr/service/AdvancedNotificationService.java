package com.example.hr.service;

import com.example.hr.dto.NotificationSendDTO;
import com.example.hr.enums.NotificationChannel;
import com.example.hr.models.Notification;
import com.example.hr.models.NotificationPreference;
import com.example.hr.models.NotificationTemplate;
import com.example.hr.models.User;
import com.example.hr.repository.NotificationPreferenceRepository;
import com.example.hr.repository.NotificationRepository;
import com.example.hr.repository.NotificationTemplateRepository;
import com.example.hr.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedNotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final InternalMessageDeliveryService internalMessageDeliveryService;
    
    @Transactional
    public void sendNotification(NotificationSendDTO dto) {
        List<User> users = userRepository.findAllById(dto.getUserIds().stream()
                .map(Long::intValue)
                .collect(Collectors.toList()));
        
        for (User user : users) {
            // Check user preferences
            if (!shouldSendNotification(user, dto.getNotificationType(), dto.getChannel())) {
                continue;
            }
            
            String message = dto.getMessage();
            String subject = dto.getSubject();
            
            // Use template if specified
            if (dto.getUseTemplate() != null && dto.getUseTemplate() && dto.getTemplateCode() != null) {
                Optional<NotificationTemplate> templateOpt = notificationTemplateRepository
                        .findByCodeAndChannel(dto.getTemplateCode(), dto.getChannel());
                
                if (templateOpt.isPresent()) {
                    NotificationTemplate template = templateOpt.get();
                    subject = replaceVariables(template.getSubject(), dto.getVariables());
                    message = replaceVariables(template.getTemplate(), dto.getVariables());
                }
            }
            
            // Send based on channel
            switch (dto.getChannel()) {
                case IN_APP:
                    sendInAppNotification(user, subject, message);
                    break;
                case EMAIL:
                    sendEmailNotification(user, subject, message);
                    break;
                case SMS:
                    sendSMSNotification(user, message);
                    break;
                case PUSH:
                    sendPushNotification(user, subject, message);
                    break;
                case SLACK:
                    internalMessageDeliveryService.sendSlack(subject, message);
                    break;
                case ZALO:
                    internalMessageDeliveryService.sendZalo(user, subject, message);
                    break;
            }
        }
    }
    
    private boolean shouldSendNotification(User user, String notificationType, NotificationChannel channel) {
        Optional<NotificationPreference> prefOpt = notificationPreferenceRepository
                .findByUserAndNotificationTypeAndChannel(user, notificationType, channel);
        
        return prefOpt.map(NotificationPreference::getEnabled).orElse(true);
    }
    
    private String replaceVariables(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
    
    @Transactional
    public void sendInAppNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(com.example.hr.enums.NotificationType.INFO);
        notification.setRead(false);
        notificationRepository.save(notification);
    }
    
    public void sendEmailNotification(User user, String subject, String message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(user.getEmail());
            email.setSubject(subject);
            email.setText(message);
            mailSender.send(email);
            log.info("Email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", user.getEmail(), e);
        }
    }
    
    public void sendSMSNotification(User user, String message) {
        // TODO: Integrate with Twilio or other SMS provider
        log.info("SMS notification to {}: {}", user.getPhoneNumber(), message);
    }
    
    public void sendPushNotification(User user, String title, String body) {
        try {
            // Assuming user has FCM token stored
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                Message message = Message.builder()
                        .setToken(user.getFcmToken())
                        .putData("title", title)
                        .putData("body", body)
                        .build();
                
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Push notification sent: {}", response);
            }
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
        }
    }
    
    @Transactional
    public void updateNotificationPreference(User user, String notificationType, 
                                            NotificationChannel channel, Boolean enabled) {
        Optional<NotificationPreference> prefOpt = notificationPreferenceRepository
                .findByUserAndNotificationTypeAndChannel(user, notificationType, channel);
        
        NotificationPreference preference;
        if (prefOpt.isPresent()) {
            preference = prefOpt.get();
        } else {
            preference = new NotificationPreference();
            preference.setUser(user);
            preference.setNotificationType(notificationType);
            preference.setChannel(channel);
        }
        
        preference.setEnabled(enabled);
        notificationPreferenceRepository.save(preference);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationPreference> getUserPreferences(User user) {
        return notificationPreferenceRepository.findByUser(user);
    }
    
    @Transactional
    public NotificationTemplate createTemplate(String code, String name, NotificationChannel channel,
                                              String subject, String template) {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setCode(code);
        notificationTemplate.setName(name);
        notificationTemplate.setChannel(channel);
        notificationTemplate.setSubject(subject);
        notificationTemplate.setTemplate(template);
        notificationTemplate.setIsActive(true);
        
        return notificationTemplateRepository.save(notificationTemplate);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getAllTemplates() {
        return notificationTemplateRepository.findByIsActiveOrderByNameAsc(true);
    }
}
