package com.ahd.backend.carcontracts.notification.controller;

import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.dto.NotificationResponse;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


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
}