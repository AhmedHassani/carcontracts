package com.ahd.backend.carcontracts.contract_installment;

import com.ahd.backend.carcontracts.contract.Contract;
import com.ahd.backend.carcontracts.contract.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractInstallmentService {

    private final ContractInstallmentRepository repository;
    private final ContractRepository contractRepository;
    @Transactional
    public ContractInstallment create(ContractInstallment installment) {
        ContractInstallment savedInstallment = repository.save(installment);

        Contract contract = installment.getContract();

        if (installment.getAmountPaid() != null && installment.getAmountPaid().compareTo(BigInteger.ZERO) > 0) {
            BigInteger currentPaid = contract.getAmountPaid() != null ? contract.getAmountPaid() : BigInteger.ZERO;
            contract.setAmountPaid(currentPaid.add(installment.getAmountPaid()));

            if (contract.getAmountPaid().compareTo(contract.getTotalAmount()) >= 0) {
                contract.setPaymentStatus("PAID");
            }

            contract.setUpdatedAt(new java.util.Date());

            contractRepository.save(contract);
        }

        return savedInstallment;
    }


    @Transactional
    public ContractInstallment update(Long id, ContractInstallment updatedInstallment) {
        ContractInstallment existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        Contract contract = existing.getContract();

        BigInteger oldPaid = existing.getAmountPaid() != null ? existing.getAmountPaid() : BigInteger.ZERO;
        BigInteger newPaid = updatedInstallment.getAmountPaid() != null ? updatedInstallment.getAmountPaid() : BigInteger.ZERO;
        BigInteger diff = newPaid.subtract(oldPaid);

        existing.setAmountPaid(newPaid);
        existing.setPaidAt(updatedInstallment.getPaidAt());
        existing.setStatus(updatedInstallment.getStatus());
        existing.setDueDate(updatedInstallment.getDueDate());

        BigInteger contractPaid = contract.getAmountPaid() != null ? contract.getAmountPaid() : BigInteger.ZERO;
        contract.setAmountPaid(contractPaid.add(diff));

        if (contract.getAmountPaid().compareTo(contract.getTotalAmount()) >= 0) {
            contract.setPaymentStatus("PAID");
        }

        contract.setUpdatedAt(new java.util.Date());

        return repository.save(existing);
    }


    @Transactional
    public void softDelete(Long id) {
        ContractInstallment installment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (installment.isDeleted()) {
            throw new RuntimeException("Installment already deleted");
        }

        installment.setDeleted(true);
        repository.save(installment);

        if (installment.getAmountPaid() != null && installment.getAmountPaid().compareTo(BigInteger.ZERO) > 0) {
            Contract contract = installment.getContract();

            BigInteger currentPaid = contract.getAmountPaid() != null ? contract.getAmountPaid() : BigInteger.ZERO;
            BigInteger newPaid = currentPaid.subtract(installment.getAmountPaid());

            contract.setAmountPaid(newPaid.max(BigInteger.ZERO)); // Prevent negative
            contract.setPaymentStatus("UNPAID");

            contract.setUpdatedAt(new java.util.Date());
            contractRepository.save(contract);
        }
    }

    public List<ContractInstallment> getAll() {
        return repository.findByDeletedFalse();
    }

    public List<ContractInstallment> getByContractId(Long contractId) {
        return repository.findByContractIdAndDeletedFalse(contractId);
    }

    public ContractInstallment getById(Long id) {
        return repository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new RuntimeException("Installment not found or deleted"));
    }
}
