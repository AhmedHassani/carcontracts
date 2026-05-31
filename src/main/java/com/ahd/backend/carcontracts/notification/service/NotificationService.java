package com.ahd.backend.carcontracts.notification.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.dto.NotificationResponse;
import com.ahd.backend.carcontracts.notification.model.Notification;
import com.ahd.backend.carcontracts.notification.repository.NotificationRepository;
import com.ahd.backend.carcontracts.util.Helper;
import com.google.api.gax.rpc.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final Helper helper;
    private final FCMService fcmService;
    private final com.ahd.backend.carcontracts.appuser.repository.UserRepository userRepo;

    /**
     * Sends a notification to one or multiple receivers based on the request.
     * Supports:
     * - Specific User IDs
     * - Specific Roles (Action will be visible to all users with this role in the
     * context)
     */
    @Transactional
    public void sendNotification(NotificationRequest request) {
        List<Notification> notificationsToSave = new ArrayList<>();
        List<String> fcmTokens = new ArrayList<>();
        // 1. Handle Specific User Targets
        if (request.hasSpecificUsers()) {
            for (Long userId : request.getTargetUserIds()) {
                Notification n = buildNotification(request);
                n.setTargetUserId(userId);
                n.setTargetRole(null);
                notificationsToSave.add(n);
            }
            // Fetch tokens for push notification
            fcmTokens.addAll(userRepo.findFcmTokensByUserIds(request.getTargetUserIds()));
        }
        // 2. Handle Role Targets
        if (request.hasTargetRoles()) {
            for (String roleName : request.getTargetRoles()) {
                Notification n = buildNotification(request);
                n.setTargetRole(roleName);
                n.setTargetUserId(null);
                notificationsToSave.add(n);
            }
            // Fetch tokens for push notification (if company context exists)
            if (request.getCompanyId() != null) {
                fcmTokens.addAll(
                        userRepo.findFcmTokensByRolesAndCompany(request.getTargetRoles(), request.getCompanyId()));
            }
        }
        if (!notificationsToSave.isEmpty()) {
            notificationRepo.saveAll(notificationsToSave);
        }
        // Send Push Notifications asynchronously (fire and forget)
        for (String token : fcmTokens) {
            fcmService.sendNotification(token, request.getTitle(), request.getMessage());
        }
    }

    private Notification buildNotification(NotificationRequest request) {
        // Get current time in Baghdad (UTC+3)
        ZoneId baghdadZone = ZoneId.of("Asia/Baghdad");
        LocalDateTime baghdadTime = ZonedDateTime.now(baghdadZone).toLocalDateTime();
        
        return Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .actionBy(request.getActionBy())
                .actionType(request.getActionType())
                .actionDate(request.getActionDate())
                .companyId(request.getCompanyId())
                .isRead(false)
                .createdAt(baghdadTime)  // Set Baghdad time explicitly
                .build();
    }

    /**
     * Get notifications for the currently logged-in user.
     * This includes:
     * - Notifications sent specifically to their User ID.
     * - Notifications sent to their Roles (within their Company or Global).
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        AppUser user = helper.getCurrentUser();
        //System.out.println("Fetching notifications for user: " + user.getId());
        Long userId = user.getId();
        List<String> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        Long companyId = null;
        if (!userRoles.contains("ROLE_SUPER_ADMIN")) {
            try {
                companyId = helper.getCurrentCompanyId();
            } catch (Exception e) {
                // Not in a company context
            }
        }
        return notificationRepo.findNotificationsForUser(userId, userRoles, companyId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        AppUser user = helper.getCurrentUser();
        List<String> userRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        Long companyId = null;
        if (!userRoles.contains("ROLE_SUPER_ADMIN")) {
            try {
                companyId = helper.getCurrentCompanyId();
            } catch (Exception e) {
                // Not in a company context
            }
        }

        return notificationRepo.countUnreadNotifications(user.getId(), userRoles, companyId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId
                ));
        notification.setRead(true);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .actionBy(n.getActionBy())
                .actionType(n.getActionType())
                .actionDate(n.getActionDate())
                .companyId(n.getCompanyId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .targetRole(n.getTargetRole())
                .targetUserId(n.getTargetUserId())
                .build();
    }
}