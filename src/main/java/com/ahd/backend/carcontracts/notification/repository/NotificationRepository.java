package com.ahd.backend.carcontracts.notification.repository;

import com.ahd.backend.carcontracts.notification.dto.NotificationWithSeenDTO;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByNotificationDateBetween(LocalDateTime start, LocalDateTime end);
    long countByNotificationDateBetween(LocalDateTime start, LocalDateTime end);
    @Query("""
        select new com.ahd.backend.carcontracts.notification.dto.NotificationWithSeenDTO(
            n.id, n.title, n.body,
            coalesce(sn.notificationSeen, false)
        )
        from AppNotification n
        left join SeenNotification sn
           on sn.appNotification.id = n.id
          and sn.appUser.id = :userId
        """)
    Page<NotificationWithSeenDTO> findAllWithSeen(@Param("userId") Long userId, Pageable pageable);

}
