package com.ahd.backend.carcontracts.contract_installment;

import com.ahd.backend.carcontracts.contract.Contract;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "contract_installment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_id", "installment_no"})
})
public class ContractInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;


    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "amount_due", nullable = false)
    private BigInteger amountDue;

    @Column(name = "amount_paid")
    private BigInteger amountPaid;

    @Column(length = 20)
    private String status;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

}
