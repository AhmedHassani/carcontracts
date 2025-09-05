package com.ahd.backend.carcontracts.notification.model;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
        name = "seen_notification",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_id"})
)
public class SeenNotification {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne @JoinColumn(name = "user_id", nullable = false)
        private AppUser appUser;

        @ManyToOne @JoinColumn(name = "notification_id", nullable = false)
        private AppNotification appNotification;

        @Column(name = "notification_seen", nullable = false)
        private boolean notificationSeen;
}
