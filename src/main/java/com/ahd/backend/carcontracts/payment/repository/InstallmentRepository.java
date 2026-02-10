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
    SELECT i.paidDate as day, SUM(i.amount) 
    FROM Installment i 
    WHERE i.status = 'PAID' AND i.paidDate BETWEEN :start AND :end AND companyId = :companyId
    GROUP BY i.paidDate
    ORDER BY i.paidDate
""")
    List<Object[]> countByDay(
                              @Param("start") LocalDate start,
                              @Param("end") LocalDate end ,
                              @Param("companyId") Long companyId);
    @Query(value = """
    SELECT FORMAT(paid_date, 'yyyy-MM') AS month, SUM(amount) AS total_amount
    FROM installments
    WHERE status = 'PAID' AND paid_date BETWEEN :start AND :end AND company_id = :companyId
    GROUP BY FORMAT(paid_date, 'yyyy-MM')
    ORDER BY month
""", nativeQuery = true)
    List<Object[]> countByMonth(@Param("start") LocalDate start,
                                @Param("end") LocalDate end,
                                @Param("companyId") Long companyId);
;



}