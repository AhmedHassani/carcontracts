package com.ahd.backend.carcontracts.contract_installment;

import com.ahd.backend.carcontracts.contract.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractInstallmentRepository extends JpaRepository<ContractInstallment, Long> {
    List<ContractInstallment> findByContractIdAndDeletedFalse(Long contractId);
    List<ContractInstallment> findByDeletedFalse();
}
