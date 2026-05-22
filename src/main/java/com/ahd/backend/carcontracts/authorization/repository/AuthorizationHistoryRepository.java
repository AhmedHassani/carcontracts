package com.ahd.backend.carcontracts.authorization.repository;

import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorizationHistoryRepository extends JpaRepository<AuthorizationHistory, Long> {
    
    List<AuthorizationHistory> findByAuthorizationIdOrderByChangeNumberDesc(Long authorizationId);
    
    Optional<AuthorizationHistory> findByAuthorizationIdAndChangeNumber(Long authorizationId, Integer changeNumber);
    
    @Query("SELECT MAX(h.changeNumber) FROM AuthorizationHistory h WHERE h.authorizationId = :authorizationId")
    Integer findMaxChangeNumberByAuthorizationId(@Param("authorizationId") Long authorizationId);
}