package com.ahd.backend.carcontracts.payment.repository;


import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.model.Installment;
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
public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByPaymentPlanIdAndCompanyId(Long paymentPlanId , Long companyId);
    Optional<Installment> findByIdAndCompanyId(Long id , Long companyId);

    List<Installment> findByStatus(InstallmentStatus status);

    List<Installment> findByPaymentPlanIdAndCompanyIdOrderByInstallmentNumber( Long paymentPlanId , Long CompnayId );


    @Query("SELECT i FROM Installment i WHERE i.dueDate < :date AND i.status = :status AND i.companyId = :companyId")
    List<Installment> findOverdueInstallmentsByCompanyId(@Param("date") LocalDate date,
                                              @Param("status") InstallmentStatus status,
                                                         @Param("companyId") Long companyId);

    @Query("""
    SELECT COALESCE(SUM(i.amount), 0) 
    FROM Installment i 
    WHERE i.status = :status 
      AND i.paidDate BETWEEN :start AND :end AND companyId = :companyId
""")
    long countByStatusAndDateRange(@Param("status") InstallmentStatus status,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end ,
                                   @Param("companyId") Long companyId);




@Query("""
    SELECT CAST(p.createdAt AS LocalDate) as day, SUM(p.intInstallment) as totalAmount
    FROM PaymentPlan p 
    WHERE p.intInstallment IS NOT NULL 
    AND p.intInstallment > 0 
    AND CAST(p.createdAt AS LocalDate) BETWEEN :start AND :end 
    AND p.companyId = :companyId
    GROUP BY CAST(p.createdAt AS LocalDate)
    ORDER BY CAST(p.createdAt AS LocalDate)
""")
List<Object[]> getIntInstallmentByDay(
    @Param("start") LocalDate start,
    @Param("end") LocalDate end,
    @Param("companyId") Long companyId
);

@Query(value = """
    SELECT FORMAT(created_at, 'yyyy-MM') as month, SUM(int_installment) as totalAmount
    FROM payment_plans
    WHERE int_installment IS NOT NULL 
    AND int_installment > 0 
    AND CAST(created_at AS DATE) BETWEEN :start AND :end 
    AND company_id = :companyId
    GROUP BY FORMAT(created_at, 'yyyy-MM')
    ORDER BY month
""", nativeQuery = true)
List<Object[]> getIntInstallmentByMonth(
    @Param("start") LocalDate start,
    @Param("end") LocalDate end,
    @Param("companyId") Long companyId
);@Query("""
    SELECT i.paidDate as day, 
           SUM(i.amount - COALESCE(i.remainingAmount, 0)) as totalAmount
    FROM Installment i 
    WHERE (i.status = 'PAID' OR i.status = 'PARTIALLY_PAID')
    AND i.paidDate BETWEEN :start AND :end 
    AND i.companyId = :companyId
    GROUP BY i.paidDate
    ORDER BY i.paidDate
""")
List<Object[]> getInstallmentPaymentsByDay(
    @Param("start") LocalDate start,
    @Param("end") LocalDate end,
    @Param("companyId") Long companyId
);

@Query(value = """
    SELECT FORMAT(paid_date, 'yyyy-MM') as month, 
           SUM(amount - COALESCE(remaining_amount, 0)) as totalAmount
    FROM installments
    WHERE (status = 'PAID' OR status = 'PARTIALLY_PAID')
    AND paid_date BETWEEN :start AND :end 
    AND company_id = :companyId
    GROUP BY FORMAT(paid_date, 'yyyy-MM')
    ORDER BY month
""", nativeQuery = true)
List<Object[]> getInstallmentPaymentsByMonth(
    @Param("start") LocalDate start,
    @Param("end") LocalDate end,
    @Param("companyId") Long companyId
);

@Query("SELECT i.companyId, COUNT(i) FROM Installment i " +
            "WHERE i.dueDate < :date AND i.status NOT IN (:excludedStatuses) " +
            "GROUP BY i.companyId")
    List<Object[]> countOverdueInstallmentsByCompany(@Param("date") LocalDate date,
            @Param("excludedStatuses") List<InstallmentStatus> excludedStatuses);


}