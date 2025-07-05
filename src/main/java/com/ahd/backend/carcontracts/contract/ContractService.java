package com.ahd.backend.carcontracts.contract;

import com.ahd.backend.carcontracts.contract_installment.ContractInstallment;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.ahd.backend.carcontracts.contract_installment.ContractInstallmentRepository;
import com.ahd.backend.carcontracts.S3.ImageStorageService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ImageStorageService imageStorageService;
    private final ContractRepository contractRepository;
    private final ContractInstallmentRepository installmentRepository;
    private final ContractImageRepository contractImageRepository;

    @Transactional
    public Contract createContractWithInstallments(ContractAndInstallmentCreateDto dto) {
        Contract savedContract = contractRepository.save(dto.getContract());

        for (ContractInstallment installment : dto.getContractInstallments()) {
            installment.setContract(savedContract);
        }
        installmentRepository.saveAll(dto.getContractInstallments());

        List<ContractImage> contractImages = dto.getContractImage().stream()
                .map(imageDto -> mapToEntity(imageDto, savedContract))
                .collect(Collectors.toList());

        contractImageRepository.saveAll(contractImages);

        return savedContract;
    }
    private ContractImage mapToEntity(ContractImageDTO dto, Contract contract) {
        return ContractImage.builder()
                .image(dto.getImage())
                .deleted(dto.isDeleted())
                .contract(contract)
                .build();
    }

    public Page<ContractResponseDTO> getAllContracts(
            String contractNumberFilter,
            LocalDate contractDateFilter,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Contract> contractPage = contractRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted"))); // soft delete filter

            if (contractNumberFilter != null && !contractNumberFilter.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("contractNumber")), "%" + contractNumberFilter.toLowerCase() + "%"));
            }

            if (contractDateFilter != null) {
                predicates.add(cb.equal(root.get("contractDate").as(LocalDate.class), contractDateFilter));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return contractPage.map(contract -> ContractResponseDTO.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractDate(contract.getContractDate())
                .carId(contract.getCarId())
                .sellerId(contract.getSellerId())
                .buyerId(contract.getBuyerId())
                .branchId(contract.getBranchId())
                .installmentAmount(contract.getInstallmentAmount())
                .daysAmountBetweenInstallments(contract.getDaysAmountBetweenInstallments())
                .saleType(contract.getSaleType())
                .totalAmount(contract.getTotalAmount())
                .amountPaid(contract.getAmountPaid())
                .paymentMethod(contract.getPaymentMethod())
                .paymentStatus(contract.getPaymentStatus())
                .createdBy(contract.getCreatedBy())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .deleted(contract.isDeleted())
                .build());
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
      //  contract.setInstallmentAmount(dto.getInstallmentAmount());
      //  contract.setDaysAmountBetweenInstallments(dto.getDaysAmountBetweenInstallments());
        contract.setSaleType(dto.getSaleType());
     //   contract.setTotalAmount(dto.getTotalAmount());
     //   contract.setAmountPaid(dto.getAmountPaid());
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
              //  .installmentAmount(updated.getInstallmentAmount())
              //  .daysAmountBetweenInstallments(updated.getDaysAmountBetweenInstallments())
                .saleType(updated.getSaleType())
              //  .totalAmount(updated.getTotalAmount())
              //  .amountPaid(updated.getAmountPaid())
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
                .installmentAmount(contract.getInstallmentAmount())
                .daysAmountBetweenInstallments(contract.getDaysAmountBetweenInstallments())
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
