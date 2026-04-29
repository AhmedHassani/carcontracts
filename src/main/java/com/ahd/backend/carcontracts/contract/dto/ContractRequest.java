package com.ahd.backend.carcontracts.contract.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ContractRequest {

    @NotNull
    private LocalDate contractDate;

    @NotNull
    private Long sellerId;

    @NotNull
    private Long buyerId;

    /** Optional – must be supplied only when payment plan method = INSTALLMENT */
    private Long guarantorId;

    @NotNull
    private Long carId;

    /** Choose ONE of the following */
    private Long paymentId;
    private Long companyId;
    private Long possessorId;

}
