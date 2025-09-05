package com.ahd.backend.carcontracts.notification.dto;

import com.ahd.backend.carcontracts.notification.model.AppNotification;

public record NotificationWithSeenDTO(
        Long id,
        String title,
        String body,
        boolean seen
) {
    public static NotificationWithSeenDTO of(AppNotification n, boolean seen) {
        return new NotificationWithSeenDTO(n.getId(), n.getTitle(), n.getBody(), seen);
    }
}
