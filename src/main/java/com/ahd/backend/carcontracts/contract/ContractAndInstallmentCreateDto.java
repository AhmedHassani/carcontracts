package com.ahd.backend.carcontracts.contract;
import com.ahd.backend.carcontracts.contract_installment.ContractInstallment;

import lombok.Data;
import java.util.List;


@Data
public class ContractAndInstallmentCreateDto {
    private Contract contract;
    private List<ContractInstallment> contractInstallments;
}
