package com.ahd.backend.carcontracts.contract;

import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractResponseDTO createContract(ContractRequestDTO dto) {
        Contract contract = Contract.builder()
                .contractNumber(dto.getContractNumber())
                .contractDate(dto.getContractDate())
                .carId(dto.getCarId())
                .sellerId(dto.getSellerId())
                .buyerId(dto.getBuyerId())
                .branchId(dto.getBranchId())
                .saleType(dto.getSaleType())
                .totalAmount(dto.getTotalAmount())
                .amountPaid(dto.getAmountPaid())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(dto.getPaymentStatus())
                .createdBy(dto.getCreatedBy())
                .createdAt(new Date())
                .updatedAt(new Date())
                .deleted(false)
                .build();

        contract = contractRepository.save(contract);

        return ContractResponseDTO.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractDate(contract.getContractDate())
                .carId(contract.getCarId())
                .sellerId(contract.getSellerId())
                .buyerId(contract.getBuyerId())
                .branchId(contract.getBranchId())
                .saleType(contract.getSaleType())
                .totalAmount(contract.getTotalAmount())
                .amountPaid(contract.getAmountPaid())
                .paymentMethod(contract.getPaymentMethod())
                .paymentStatus(contract.getPaymentStatus())
                .createdBy(contract.getCreatedBy())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .deleted(contract.isDeleted())
                .build();
    }

    public List<ContractResponseDTO> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .filter(contract -> !contract.isDeleted()) // skip soft-deleted
                .map(contract -> ContractResponseDTO.builder()
                        .id(contract.getId())
                        .contractNumber(contract.getContractNumber())
                        .contractDate(contract.getContractDate())
                        .carId(contract.getCarId())
                        .sellerId(contract.getSellerId())
                        .buyerId(contract.getBuyerId())
                        .branchId(contract.getBranchId())
                        .saleType(contract.getSaleType())
                        .totalAmount(contract.getTotalAmount())
                        .amountPaid(contract.getAmountPaid())
                        .paymentMethod(contract.getPaymentMethod())
                        .paymentStatus(contract.getPaymentStatus())
                        .createdBy(contract.getCreatedBy())
                        .createdAt(contract.getCreatedAt())
                        .updatedAt(contract.getUpdatedAt())
                        .deleted(contract.isDeleted())
                        .build())
                .collect(Collectors.toList());
    }
    public void softDeleteContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        contract.setDeleted(true);
        contract.setUpdatedAt(new Date());
        contractRepository.save(contract);
    }

    public ContractResponseDTO updateContract(Long id, ContractRequestDTO dto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        if (contract.isDeleted()) {
            throw new IllegalStateException("Cannot update a deleted contract.");
        }

        contract.setContractNumber(dto.getContractNumber());
        contract.setContractDate(dto.getContractDate());
        contract.setCarId(dto.getCarId());
        contract.setSellerId(dto.getSellerId());
        contract.setBuyerId(dto.getBuyerId());
        contract.setBranchId(dto.getBranchId());
        contract.setSaleType(dto.getSaleType());
        contract.setTotalAmount(dto.getTotalAmount());
        contract.setAmountPaid(dto.getAmountPaid());
        contract.setPaymentMethod(dto.getPaymentMethod());
        contract.setPaymentStatus(dto.getPaymentStatus());
        contract.setCreatedBy(dto.getCreatedBy());
        contract.setUpdatedAt(new Date());

        Contract updated = contractRepository.save(contract);

        return ContractResponseDTO.builder()
                .id(updated.getId())
                .contractNumber(updated.getContractNumber())
                .contractDate(updated.getContractDate())
                .carId(updated.getCarId())
                .sellerId(updated.getSellerId())
                .buyerId(updated.getBuyerId())
                .branchId(updated.getBranchId())
                .saleType(updated.getSaleType())
                .totalAmount(updated.getTotalAmount())
                .amountPaid(updated.getAmountPaid())
                .paymentMethod(updated.getPaymentMethod())
                .paymentStatus(updated.getPaymentStatus())
                .createdBy(updated.getCreatedBy())
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .deleted(updated.isDeleted())
                .build();
    }
    public ContractResponseDTO getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        if (contract.isDeleted()) {
            throw new ResourceNotFoundException("Contract is deleted or does not exist with id: " + id);
        }

        return ContractResponseDTO.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractDate(contract.getContractDate())
                .carId(contract.getCarId())
                .sellerId(contract.getSellerId())
                .buyerId(contract.getBuyerId())
                .branchId(contract.getBranchId())
                .saleType(contract.getSaleType())
                .totalAmount(contract.getTotalAmount())
                .amountPaid(contract.getAmountPaid())
                .paymentMethod(contract.getPaymentMethod())
                .paymentStatus(contract.getPaymentStatus())
                .createdBy(contract.getCreatedBy())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .deleted(contract.isDeleted())
                .build();
    }

}
