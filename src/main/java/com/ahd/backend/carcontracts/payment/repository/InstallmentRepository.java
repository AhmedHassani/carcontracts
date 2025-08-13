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
import java.util.List;
import java.util.Optional;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByPaymentPlanId(Long paymentPlanId);

    List<Installment> findByStatus(InstallmentStatus status);

    List<Installment> findByPaymentPlanIdOrderByInstallmentNumber(Long paymentPlanId);

    @Query("SELECT i FROM Installment i WHERE i.dueDate BETWEEN :startDate AND :endDate")
    List<Installment> findByDueDateBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Installment i WHERE i.dueDate < :date AND i.status = :status")
    List<Installment> findOverdueInstallments(@Param("date") LocalDate date,
                                              @Param("status") InstallmentStatus status);

    @Query("SELECT i FROM Installment i WHERE i.paymentPlan.id = :paymentPlanId AND i.status = :status")
    List<Installment> findByPaymentPlanIdAndStatus(@Param("paymentPlanId") Long paymentPlanId,
                                                   @Param("status") InstallmentStatus status);

    @Query("SELECT i FROM Installment i WHERE i.paymentPlan.id = :paymentPlanId AND i.installmentNumber = :installmentNumber")
    Optional<Installment> findByPaymentPlanIdAndInstallmentNumber(@Param("paymentPlanId") Long paymentPlanId,
                                                                  @Param("installmentNumber") Integer installmentNumber);

    @Query("SELECT COUNT(i) FROM Installment i WHERE i.paymentPlan.id = :paymentPlanId AND i.status = :status")
    Long countByPaymentPlanIdAndStatus(@Param("paymentPlanId") Long paymentPlanId,
                                       @Param("status") InstallmentStatus status);

    @Query("SELECT SUM(i.amount) FROM Installment i WHERE i.paymentPlan.id = :paymentPlanId AND i.status = :status")
    BigDecimal sumAmountByPaymentPlanIdAndStatus(@Param("paymentPlanId") Long paymentPlanId,
                                                 @Param("status") InstallmentStatus status);

    @Query("SELECT i FROM Installment i WHERE i.paymentReference = :paymentReference")
    Optional<Installment> findByPaymentReference(@Param("paymentReference") String paymentReference);

    @Query("SELECT i FROM Installment i WHERE i.dueDate = :dueDate AND i.status = :status")
    List<Installment> findByDueDateAndStatus(@Param("dueDate") LocalDate dueDate,
                                             @Param("status") InstallmentStatus status);

    @Query("SELECT i FROM Installment i JOIN i.paymentPlan p WHERE p.status = :planStatus AND i.status = :installmentStatus")
    List<Installment> findByPaymentPlanStatusAndInstallmentStatus(@Param("planStatus") PaymentStatus planStatus,
                                                                  @Param("installmentStatus") InstallmentStatus installmentStatus);

    @Modifying
    @Query("UPDATE Installment i SET i.status = :status WHERE i.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") InstallmentStatus status);

    @Modifying
    @Query("UPDATE Installment i SET i.status = :newStatus WHERE i.dueDate < :date AND i.status = :currentStatus")
    int updateOverdueInstallments(@Param("date") LocalDate date,
                                  @Param("currentStatus") InstallmentStatus currentStatus,
                                  @Param("newStatus") InstallmentStatus newStatus);

    @Query("SELECT i FROM Installment i WHERE i.paymentPlan.id = :paymentPlanId AND i.status != 'PAID' ORDER BY i.installmentNumber")
    List<Installment> findUnpaidInstallmentsByPaymentPlanId(@Param("paymentPlanId") Long paymentPlanId);

    @Query("SELECT i FROM Installment i WHERE i.paymentPlan.id = :paymentPlanId AND i.status = 'PAID' ORDER BY i.paidDate DESC")
    List<Installment> findPaidInstallmentsByPaymentPlanId(@Param("paymentPlanId") Long paymentPlanId);

    @Query("""
    SELECT COUNT(i) 
    FROM Installment i 
    WHERE i.status = :status 
      AND i.paidDate BETWEEN :start AND :end
""")
    long countByStatusAndDateRange(@Param("status") InstallmentStatus status,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

}