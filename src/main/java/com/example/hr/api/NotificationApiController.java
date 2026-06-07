package com.example.hr.api;

import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.service.CloudStorageFacade;
import com.example.hr.service.NotificationService;
import com.example.hr.service.AuthUserHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Push notification management")
public class NotificationApiController {

    private final UserRepository userRepository;
    private final CloudStorageFacade cloudStorageFacade;
    private final NotificationService notificationService;
    private final AuthUserHelper authUserHelper;

    public NotificationApiController(UserRepository userRepository,
                                      CloudStorageFacade cloudStorageFacade,
                                      NotificationService notificationService,
                                      AuthUserHelper authUserHelper) {
        this.userRepository = userRepository;
        this.cloudStorageFacade = cloudStorageFacade;
        this.notificationService = notificationService;
        this.authUserHelper = authUserHelper;
    }

    /** Unread count â€” dÃ¹ng cho badge polling */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Láº¥y sá»‘ thÃ´ng bÃ¡o chÆ°a Ä‘á»c")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        long count = user != null ? notificationService.countUnread(user) : 0;
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** Mark all read via API */
    @PutMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "ÄÃ¡nh dáº¥u táº¥t cáº£ Ä‘Ã£ Ä‘á»c")
    public ResponseEntity<Map<String, String>> markAllRead(Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user != null) notificationService.markAllRead(user);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * Frontend gá»i endpoint nÃ y sau khi láº¥y Ä‘Æ°á»£c FCM token tá»« Firebase SDK.
     */
    @PostMapping("/register-token")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "ÄÄƒng kÃ½ FCM token cho push notifications")
    public ResponseEntity<Map<String, String>> registerToken(
            @RequestBody Map<String, String> body,
            Principal principal) {
        String fcmToken = body.get("fcmToken");
        if (fcmToken == null || fcmToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "fcmToken is required"));
        }

        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            user.setFcmToken(fcmToken);
            userRepository.save(user);
        });

        return ResponseEntity.ok(Map.of("status", "registered"));
    }

    /**
     * XÃ³a FCM token khi user logout.
     */
    @DeleteMapping("/unregister-token")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "XÃ³a FCM token khi logout")
    public ResponseEntity<Void> unregisterToken(Principal principal) {
        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            user.setFcmToken(null);
            userRepository.save(user);
        });
        return ResponseEntity.noContent().build();
    }

    /**
     * Gá»­i test notification Ä‘áº¿n user hiá»‡n táº¡i.
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gá»­i test push notification")
    public ResponseEntity<Map<String, Object>> sendTest(
            @RequestBody Map<String, String> body,
            Principal principal) {
        String targetUsername = body.getOrDefault("username", principal.getName());
        User target = userRepository.findByUsername(targetUsername).orElse(null);

        if (target == null || target.getFcmToken() == null) {
            return ResponseEntity.ok(Map.of(
                    "status", "skipped",
                    "reason", "User khÃ´ng cÃ³ FCM token"));
        }

        cloudStorageFacade.pushNotification(
                target.getFcmToken(),
                "ðŸ”” Test Notification",
                "ÄÃ¢y lÃ  test push notification tá»« HRMS Admin",
                Map.of("type", "TEST"));

        return ResponseEntity.ok(Map.of("status", "sent", "to", targetUsername));
    }

    /**
     * Broadcast thÃ´ng bÃ¡o Ä‘áº¿n táº¥t cáº£ nhÃ¢n viÃªn qua Firebase.
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Broadcast thÃ´ng bÃ¡o Ä‘áº¿n táº¥t cáº£ nhÃ¢n viÃªn")
    public ResponseEntity<Map<String, String>> broadcast(
            @RequestBody Map<String, String> body) {
        String title   = body.getOrDefault("title", "ThÃ´ng bÃ¡o");
        String content = body.getOrDefault("content", "");
        cloudStorageFacade.broadcastAnnouncement(title, content);
        return ResponseEntity.ok(Map.of("status", "broadcast_sent"));
    }

    /**
     * Láº¥y tráº¡ng thÃ¡i táº¥t cáº£ cloud services.
     */
    @GetMapping("/cloud-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kiá»ƒm tra tráº¡ng thÃ¡i cloud services")
    public ResponseEntity<Map<String, Object>> cloudStatus() {
        return ResponseEntity.ok(cloudStorageFacade.getHealthStatus());
    }
}


