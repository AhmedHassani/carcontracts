package com.ahd.backend.carcontracts.notification.repository;

import com.ahd.backend.carcontracts.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Fetch notifications for a specific user OR for their roles (within their
    // company or global)
    // 1. Match specific targetUserId
    // 2. OR Match targetRole IN keys AND (companyId == userCompanyId OR companyId
    // IS NULL)
    @Query("SELECT n FROM Notification n " +
            "WHERE (n.targetUserId = :userId) " +
            "OR (n.targetRole IN :roles AND (n.companyId = :companyId OR n.companyId IS NULL))")
    Page<Notification> findNotificationsForUser(@Param("userId") Long userId,
            @Param("roles") List<String> roles,
            @Param("companyId") Long companyId,
            Pageable pageable);

    // For counting unread
    @Query("SELECT COUNT(n) FROM Notification n " +
            "WHERE n.isRead = false AND " +
            "((n.targetUserId = :userId) " +
            "OR (n.targetRole IN :roles AND (n.companyId = :companyId OR n.companyId IS NULL)))")
    long countUnreadNotifications(@Param("userId") Long userId,
            @Param("roles") List<String> roles,
            @Param("companyId") Long companyId);
}