package com.ahd.backend.carcontracts.payment.repository;


import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {

    List<PaymentPlan> findByStatus(PaymentStatus status);

    List<PaymentPlan> findByPaymentType(PaymentType paymentType);

    @Query("SELECT p FROM PaymentPlan p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentPlan> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM PaymentPlan p WHERE p.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<PaymentPlan> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                               @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT p FROM PaymentPlan p WHERE p.status = :status AND p.paymentType = :paymentType")
    List<PaymentPlan> findByStatusAndPaymentType(@Param("status") PaymentStatus status,
                                                 @Param("paymentType") PaymentType paymentType);

    @Query("SELECT COUNT(p) FROM PaymentPlan p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT SUM(p.totalAmount) FROM PaymentPlan p WHERE p.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM PaymentPlan p LEFT JOIN FETCH p.installments WHERE p.id = :id")
    Optional<PaymentPlan> findByIdWithInstallments(@Param("id") Long id);

    @Query("""
    SELECT COUNT(p)
    FROM PaymentPlan p
    WHERE p.status = :status
      AND p.createdAt BETWEEN :start AND :end
""")
    long countByStatusAndDateRange(@Param("status") PaymentStatus status,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);


}