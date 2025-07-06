package com.ahd.backend.carcontracts.contract_installment;

import com.ahd.backend.carcontracts.contract.Contract;
import com.ahd.backend.carcontracts.contract.ContractRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
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
        if (contract == null || contract.getId() == null) {
            throw new IllegalArgumentException("Contract must not be null and must have an ID");
        }

        Contract existingContract = contractRepository.findByIdAndDeletedFalse(contract.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found or deleted"));
        BigInteger amountDue = installment.getAmountDue();

        Integer currentInstallments = existingContract.getInstallmentAmount();
        BigInteger currentTotalAmount = existingContract.getTotalAmount();

        existingContract.setInstallmentAmount(currentInstallments + 1);
        existingContract.setTotalAmount(currentTotalAmount.add(amountDue));


        existingContract.setUpdatedAt(new java.util.Date());
        contractRepository.save(existingContract);

        return savedInstallment;
    }

    @Transactional
    public ContractInstallment update(Long id, ContractInstallment updatedInstallment) {

        ContractInstallment existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment not found, id = " + id));

        Contract contract = contractRepository.findByIdAndDeletedFalse(existing.getContract().getId())
                .orElseThrow(() -> new RuntimeException("Contract not found or deleted"));

        BigInteger oldPaid = existing.getAmountPaid() != null ? existing.getAmountPaid() : BigInteger.ZERO;
        BigInteger newPaid = updatedInstallment.getAmountPaid() != null ? updatedInstallment.getAmountPaid() : BigInteger.ZERO;
        BigInteger diff    = newPaid.subtract(oldPaid);

        existing.setAmountPaid(newPaid);
        existing.setPaidAt(updatedInstallment.getPaidAt());
        existing.setStatus(updatedInstallment.getStatus());
        existing.setDueDate(updatedInstallment.getDueDate());

        BigInteger contractPaid = contract.getAmountPaid() != null ? contract.getAmountPaid() : BigInteger.ZERO;
        contract.setAmountPaid(contractPaid.add(diff));

        if (contract.getAmountPaid().compareTo(contract.getTotalAmount()) >= 0) {
            contract.setPaymentStatus("PAID");
        } else {
            contract.setPaymentStatus("PARTIAL");
        }

        contract.setUpdatedAt(new java.util.Date());
        contractRepository.save(contract);

        return repository.save(existing);
    }

    @Transactional
    public void softDelete(Long id) {

        ContractInstallment installment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment not found, id = " + id));

        if (installment.isDeleted()) {
            throw new RuntimeException("Installment already deleted");
        }

        installment.setDeleted(true);
        repository.save(installment);

//        if (installment.getAmountPaid() != null && installment.getAmountPaid().compareTo(BigInteger.ZERO) > 0) {

            Contract contract = contractRepository.findByIdAndDeletedFalse(
                    installment.getContract().getId()
            ).orElseThrow(() -> new RuntimeException("Related contract not found or deleted"));

            BigInteger currentPaid = contract.getAmountPaid() != null ? contract.getAmountPaid() : BigInteger.ZERO;
            BigInteger newPaid     = currentPaid.subtract(installment.getAmountPaid());
            contract.setAmountPaid(newPaid.max(BigInteger.ZERO));

            Integer currentInst = contract.getInstallmentAmount();
            contract.setInstallmentAmount(Math.max(currentInst - 1, 0));

            if (contract.getAmountPaid().compareTo(contract.getTotalAmount()) >= 0) {
                contract.setPaymentStatus("PAID");
            } else if (contract.getAmountPaid().compareTo(BigInteger.ZERO) > 0) {
                contract.setPaymentStatus("PARTIAL");
            } else {
                contract.setPaymentStatus("UNPAID");
            }

            contract.setUpdatedAt(new java.util.Date());
            contractRepository.save(contract);
        //}
    }



//    public List<ContractInstallment> getAll() {
//        return repository.findByDeletedFalse();
//    }

    public List<ContractInstallment> getByContractId(Long contractId) {
        return repository.findByContractIdAndDeletedFalse(contractId);
    }

    public ContractInstallment getById(Long id) {
        return repository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new RuntimeException("Installment not found or deleted"));
    }
}
