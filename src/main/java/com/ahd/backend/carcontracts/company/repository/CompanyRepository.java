package com.ahd.backend.carcontracts.company.repository;



import com.ahd.backend.carcontracts.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> , JpaSpecificationExecutor<Company> {
    List<Company> findByExpirationDateBefore(LocalDate date);


        @Query(value = """
        SELECT 
            COUNT(c1.company_id) AS totalCount,
            (
                SELECT COUNT(c2.company_id)
                FROM dbo.company c2
                WHERE 
                    c2.expiration_date > CAST(GETDATE() AS DATE)
                    AND c2.subscription_date >= :startDate
                    AND c2.subscription_date <  DATEADD(day, 1, :endDate)
            ) AS activeCount
        FROM dbo.company c1
        WHERE 
            c1.subscription_date >= :startDate
            AND c1.subscription_date <  DATEADD(day, 1, :endDate)
        """, nativeQuery = true)
        CompanyStatsProjection getCompanyStats(
                @Param("startDate") java.time.LocalDate startDate,
                @Param("endDate")   java.time.LocalDate endDate
        );

    @Query(value = "SELECT * FROM company WHERE company_id = :id", nativeQuery = true)
    Optional<Company> findAnyById(@Param("id") Long id);

    Optional<Company> findByIdAndDeletedFalseAndExpirationDateGreaterThanEqual(Long id, LocalDate today);



List<Company> findByDeletedFalse();
    
    // Get companies with expiring subscriptions
    @Query("SELECT c FROM Company c WHERE c.deleted = false " +
           "AND c.expirationDate <= :endDate")
    List<Company> findCompaniesWithExpiringSubscription(@Param("endDate") LocalDate endDate);
    
    // Get companies with expired subscriptions
    @Query("SELECT c FROM Company c WHERE c.deleted = false " +
           "AND c.expirationDate < :today")
    List<Company> findCompaniesWithExpiredSubscription(@Param("today") LocalDate today);
    
    // Get companies expiring within date range
    @Query("SELECT c FROM Company c WHERE c.deleted = false " +
           "AND c.expirationDate BETWEEN :startDate AND :endDate")
    List<Company> findCompaniesExpiringBetween(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    // Count companies by expiration status
    @Query("SELECT COUNT(c) FROM Company c WHERE c.deleted = false " +
           "AND c.expirationDate < :today")
    long countExpiredCompanies(@Param("today") LocalDate today);
    
    @Query("SELECT COUNT(c) FROM Company c WHERE c.deleted = false " +
           "AND c.expirationDate BETWEEN :startDate AND :endDate")
    long countCompaniesExpiringBetween(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

                                        @Query("SELECT MAX(c.code) FROM Company c")
    String findMaxCode();
    
    Optional<Company> findTopByOrderByCodeDesc();

}


