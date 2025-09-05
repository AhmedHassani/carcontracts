package com.ahd.backend.carcontracts.notification.repository;

import com.ahd.backend.carcontracts.notification.dto.NotificationWithSeenDTO;
import com.ahd.backend.carcontracts.notification.model.SeenNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SeenNotificationRepository extends JpaRepository<SeenNotification, Long> {

    boolean existsByAppUserIdAndAppNotificationId(Long appUserId, Long notificationId);

    Optional<SeenNotification> findByAppUserIdAndAppNotificationId(Long appUserId, Long notificationId);
    Optional<SeenNotification> findByAppUserId(Long appUserId);

    List<SeenNotification> findAllByAppNotificationId(Long notificationId);

    long countByAppNotificationIdAndNotificationSeenTrue(Long notificationId);
    List<SeenNotification> findByAppUserIdAndAppNotificationIdIn(Long userId, Collection<Long> notificationIds);
    @Modifying
    @Query("update SeenNotification sn set sn.notificationSeen = true where sn.id = :id and sn.notificationSeen = false")
    int markSeenById(@Param("id") Long id);

    @Modifying
    @Query("""
           update SeenNotification sn
           set sn.notificationSeen = true
           where sn.appUser.id = :userId and sn.notificationSeen = false
           """)
    int markAllSeenForUser(@Param("userId") Long userId);

}

