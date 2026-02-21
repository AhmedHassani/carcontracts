package com.ahd.backend.carcontracts.appuser.repository;


import java.util.Optional;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    @Query(value = """
        SELECT 
            COUNT(u1.id) AS totalCount,
            (
                SELECT COUNT(u2.id)
                FROM dbo.app_user u2
                INNER JOIN dbo.company_user cu ON cu.user_id = u2.id
                INNER JOIN dbo.company c ON c.company_id = cu.company_id
                WHERE 
                    c.expiration_date > CAST(GETDATE() AS DATE)
                    AND CAST(u2.created_at AS DATE) BETWEEN :startDate AND :endDate
            ) AS activeCount
        FROM dbo.app_user u1
        WHERE CAST(u1.created_at AS DATE) BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    UserStatsProjection getUserStats(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate")   java.time.LocalDate endDate
    );
    @Query("SELECT u.fcmToken FROM AppUser u WHERE u.id IN :userIds AND u.fcmToken IS NOT NULL")
    List<String> findFcmTokensByUserIds(@Param("userIds") List<Long> userIds);

    @Query("SELECT u.fcmToken FROM AppUser u " +
            "JOIN u.roles r " +
            "JOIN CompanyUser cu ON cu.user.id = u.id " +
            "WHERE r.name IN :roleNames AND cu.company.id = :companyId AND u.fcmToken IS NOT NULL")
    List<String> findFcmTokensByRolesAndCompany(@Param("roleNames") List<String> roleNames,
            @Param("companyId") Long companyId);
}

