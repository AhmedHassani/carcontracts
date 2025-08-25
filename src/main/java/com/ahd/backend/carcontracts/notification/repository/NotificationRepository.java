package com.ahd.backend.carcontracts.notification.repository;

import com.ahd.backend.carcontracts.notification.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByNotificationDateBetween(LocalDateTime start, LocalDateTime end);
    long countByNotificationDateBetween(LocalDateTime start, LocalDateTime end);
}
