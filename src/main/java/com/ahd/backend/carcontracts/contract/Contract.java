package com.ahd.backend.carcontracts.contract;

import com.ahd.backend.carcontracts.branch.Branch;
import com.ahd.backend.carcontracts.car.Car;
import com.ahd.backend.carcontracts.person.Person;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Person seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Person buyer;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "branch_id")
     private Branch branch;

    @Column(name = "sale_type", nullable = false, length = 20)
    private String saleType;

    @Column(name = "total_amount")
    private BigInteger totalAmount;

    @Column(name = "amount_paid")
    private BigInteger amountPaid;

    @Column(name = "installment_Amount")
    private int installmentAmount;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "days_amount_between_installments")
    private Long daysAmountBetweenInstallments;
}
