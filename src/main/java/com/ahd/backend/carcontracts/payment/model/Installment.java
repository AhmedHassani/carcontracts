package com.ahd.backend.carcontracts.payment.model;

import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "installments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE installments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Installment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_plan_id")
    @JsonBackReference
    private PaymentPlan paymentPlan;

    @Column(name = "installment_number")
    private Integer installmentNumber;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(nullable = false)
    private boolean deleted = false;
}
