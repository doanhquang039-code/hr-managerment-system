package com.example.hr.api;

import com.example.hr.models.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.CloudStorageFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Push notification management")
public class NotificationApiController {

    private final UserRepository userRepository;
    private final CloudStorageFacade cloudStorageFacade;

    public NotificationApiController(UserRepository userRepository,
                                      CloudStorageFacade cloudStorageFacade) {
        this.userRepository = userRepository;
        this.cloudStorageFacade = cloudStorageFacade;
    }

    /**
     * Frontend gọi endpoint này sau khi lấy được FCM token từ Firebase SDK.
     */
    @PostMapping("/register-token")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đăng ký FCM token cho push notifications")
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
     * Xóa FCM token khi user logout.
     */
    @DeleteMapping("/unregister-token")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa FCM token khi logout")
    public ResponseEntity<Void> unregisterToken(Principal principal) {
        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            user.setFcmToken(null);
            userRepository.save(user);
        });
        return ResponseEntity.noContent().build();
    }

    /**
     * Gửi test notification đến user hiện tại.
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gửi test push notification")
    public ResponseEntity<Map<String, Object>> sendTest(
            @RequestBody Map<String, String> body,
            Principal principal) {
        String targetUsername = body.getOrDefault("username", principal.getName());
        User target = userRepository.findByUsername(targetUsername).orElse(null);

        if (target == null || target.getFcmToken() == null) {
            return ResponseEntity.ok(Map.of(
                    "status", "skipped",
                    "reason", "User không có FCM token"));
        }

        cloudStorageFacade.pushNotification(
                target.getFcmToken(),
                "🔔 Test Notification",
                "Đây là test push notification từ HRMS Admin",
                Map.of("type", "TEST"));

        return ResponseEntity.ok(Map.of("status", "sent", "to", targetUsername));
    }

    /**
     * Broadcast thông báo đến tất cả nhân viên qua Firebase.
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Broadcast thông báo đến tất cả nhân viên")
    public ResponseEntity<Map<String, String>> broadcast(
            @RequestBody Map<String, String> body) {
        String title   = body.getOrDefault("title", "Thông báo");
        String content = body.getOrDefault("content", "");
        cloudStorageFacade.broadcastAnnouncement(title, content);
        return ResponseEntity.ok(Map.of("status", "broadcast_sent"));
    }

    /**
     * Lấy trạng thái tất cả cloud services.
     */
    @GetMapping("/cloud-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kiểm tra trạng thái cloud services")
    public ResponseEntity<Map<String, Object>> cloudStatus() {
        return ResponseEntity.ok(cloudStorageFacade.getHealthStatus());
    }
}
