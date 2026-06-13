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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('GET_NOTIFICATTIONS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "actionDate"));
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('GET_NOTIFICATTIONS') or hasRole('SUPER_ADMIN')")
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
    @PreAuthorize("hasAuthority('GET_NOTIFICATTIONS') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // ✅ Register FCM Token endpoint - Using simple Map response
  @PostMapping("/register-token")
public ResponseEntity<Map<String, Object>> registerFcmToken(@RequestBody Map<String, String> request) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        String token = request.get("token");
        log.info("📱 Registering token request received");
        
        // Get current logged-in user
        AppUser currentUser = helper.getCurrentUser();
        if (currentUser == null) {
            log.warn("⚠️ No user authenticated");
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        log.info("👤 User found: {} (ID: {}, Username: {})", 
            currentUser.getEmail(), currentUser.getId(), currentUser.getUsername());
        
        // Check if user is Super Admin
        boolean isSuperAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_SUPER_ADMIN"));
        log.info("👑 Is Super Admin: {}", isSuperAdmin);
        
        if (token != null && !token.isEmpty()) {
            log.info("💾 Saving token for user ID: {}", currentUser.getId());
            log.info("📝 Token preview: {}", token.substring(0, Math.min(token.length(), 50)) + "...");
            
            // Save token to user record
            currentUser.setFcmToken(token);
            
            // Try to save and log the result
            AppUser savedUser = userRepository.save(currentUser);
            log.info("✅ User saved successfully, token in DB: {}", savedUser.getFcmToken() != null);
            
            response.put("success", true);
            response.put("message", "Token registered successfully");
            response.put("userId", currentUser.getId());
            response.put("isSuperAdmin", isSuperAdmin);
            return ResponseEntity.ok(response);
        } else {
            log.warn("⚠️ Received empty or null token");
            response.put("success", false);
            response.put("message", "Invalid token");
            return ResponseEntity.badRequest().body(response);
        }
        
    } catch (Exception e) {
        log.error("❌ Error registering token: {}", e.getMessage(), e);
        
        // Log the root cause
        Throwable rootCause = e;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        log.error("❌ Root cause: {}", rootCause.getMessage());
        
        response.put("success", false);
        response.put("message", "Internal server error: " + e.getMessage());
        response.put("rootCause", rootCause.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
}