package com.ahd.backend.carcontracts.payment.repository;


import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {

//    Page<PaymentPlan> findByCompanyId(Long companyId);
    Optional<PaymentPlan> findByIdAndCompanyId(Long id , Long companyId);

//    List<PaymentPlan> findByStatusAndCompanyId(PaymentStatus status , Long companyId);


    @Query("""
       SELECT p
       FROM PaymentPlan p
       LEFT JOIN FETCH p.installments
       WHERE p.id = :id
         AND p.companyId = :companyId
       """)
    Optional<PaymentPlan> findByIdAndCompanyIdWithInstallments(@Param("id") Long id,
                                                               @Param("companyId") Long companyId);

    @Query("""
       SELECT COUNT(p)
       FROM PaymentPlan p
       WHERE p.status = :status
         AND p.createdAt BETWEEN :start AND :end
         AND p.companyId = :companyId
       """)
    long countByStatusAndDateRange(@Param("status") PaymentStatus status,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("companyId") Long companyId);
    @Query("""
       SELECT COUNT(p)
       FROM PaymentPlan p
       WHERE p.status = :status
         AND p.complete_date BETWEEN :start AND :end
         AND p.companyId = :companyId
       """)
    long countByStatusAndDateRangeCompleted(@Param("status") PaymentStatus status,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end,
                                   @Param("companyId") Long companyId);

    @Query("""
       SELECT SUM(p.totalAmount)
       FROM PaymentPlan p
       WHERE p.status = :status
         AND p.complete_date BETWEEN :start AND :end
         AND p.companyId = :companyId
       """)
    Long summationByStatusAndDateRangeCompleted(@Param("status") PaymentStatus status,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end,
                                            @Param("companyId") Long companyId);

}