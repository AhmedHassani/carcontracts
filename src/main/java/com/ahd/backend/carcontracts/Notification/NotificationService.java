package com.ahd.backend.carcontracts.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseService firebaseService;
    private final NotificationRepository notificationRepository;

    public NotificationResponseDTO createNotification(NotificationRequestDTO dto) {

        Notification notification = Notification.builder()
                .message(dto.getMessage())
                .link(dto.getLink())
                .build();

        notification = notificationRepository.save(notification);

        if (dto.getFirebaseToken() != null) {
            try {
                firebaseService.sendNotification(
                        dto.getFirebaseToken(),
                        "New Notification",
                        dto.getMessage()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .is_read(notification.getIsRead())
                .build();
    }

    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(notification -> NotificationResponseDTO.builder()
                        .id(notification.getId())
                        .message(notification.getMessage())
                        .link(notification.getLink())
                        .createdAt(notification.getCreatedAt())
                        .is_read(notification.getIsRead())
                        .build())
                .collect(Collectors.toList());
    }

    public NotificationResponseDTO getNotificationById(Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);

        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .is_read(notification.getIsRead())
                .build();
    }
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id);

        notification.setIsRead(true);
        notificationRepository.save(notification);

        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .is_read(notification.getIsRead())
                .build();
    }

}
