package com.ahd.backend.carcontracts.notification.controller;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.dto.NotificationResponse;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.util.Helper;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${application.api.base-path}/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final Helper helper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "actionDate"));
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Test Notification");
        request.setMessage("This is a test message");
        request.setActionBy("SYSTEM");
        request.setActionType("NEW_CONTRACT");
        request.setActionDate(LocalDateTime.now());
        request.setCompanyId(1001L);
        request.setTargetUserIds(List.of(4054L));
        request.setTargetRoles(List.of("EMPLOYEE"));
        notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // ✅ Register FCM Token endpoint - Using simple Map response
    @PostMapping("/register-token")
    public ResponseEntity<Map<String, Object>> registerFcmToken(@RequestBody Map<String, String> request) {
       //log.info("📱 Registering FCM token endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = request.get("token");
           //log.info("📱 Token received: {}", token != null ? token.substring(0, Math.min(token.length(), 30)) + "..." : "null");
            
            // Get current//logged-in user
            AppUser currentUser = helper.getCurrentUser();
            if (currentUser == null) {
               //log.warn("⚠️ No user authenticated");
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
           //log.info("👤 User found: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            
            if (token != null && !token.isEmpty()) {
                // Save token to user record
                currentUser.setFcmToken(token);
                userRepository.save(currentUser);
               //log.info("✅ FCM token saved successfully for user: {}", currentUser.getEmail());
               //log.info("📱 Token type: {}", token.contains(":APA91b") ? "Mobile Token" : "Web Token");
                
                response.put("success", true);
                response.put("message", "Token registered successfully");
                response.put("tokenType", token.contains(":APA91b") ? "mobile" : "web");
                return ResponseEntity.ok(response);
            } else {
               //log.warn("⚠️ Received empty or null token");
                response.put("success", false);
                response.put("message", "Invalid token");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
           //log.error("❌ Error registering token: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}