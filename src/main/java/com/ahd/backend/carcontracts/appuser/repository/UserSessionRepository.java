package com.ahd.backend.carcontracts.appuser.repository;

import com.ahd.backend.carcontracts.appuser.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<UserSession> findByRefreshToken(String refreshToken);
    
    Optional<UserSession> findByAccessToken(String accessToken); // ADD THIS
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    void deactivateAllSessionsForUser(@Param("userId") Long userId);
}