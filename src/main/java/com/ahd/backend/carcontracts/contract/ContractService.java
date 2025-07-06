package com.ahd.backend.carcontracts.contract;

import com.ahd.backend.carcontracts.S3.ImageStorageService;
import com.ahd.backend.carcontracts.branch.Branch;
import com.ahd.backend.carcontracts.branch.BranchRepository;
import com.ahd.backend.carcontracts.car.Car;
import com.ahd.backend.carcontracts.car.CarRepository;
import com.ahd.backend.carcontracts.contract_installment.ContractInstallment;
import com.ahd.backend.carcontracts.contract_installment.ContractInstallmentRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.person.Person;
import com.ahd.backend.carcontracts.person.PersonRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository            contractRepository;
    private final ContractInstallmentRepository installmentRepository;
    private final ContractImageRepository       contractImageRepository;
    private final CarRepository                 carRepository;
    private final PersonRepository              personRepository;
    private final BranchRepository              branchRepository;
    private final ImageStorageService           imageStorageService;


    @Transactional
    public Contract createContractWithInstallments(ContractAndInstallmentCreateDto dto) {

        ContractDTO c = dto.getContract();

        Contract contract = Contract.builder()
                .contractNumber(c.getContractNumber())
                .contractDate(c.getContractDate())
                .car(   carRepository   .findById(c.getCarId()   ).orElseThrow(() -> new ResourceNotFoundException("Car not found")))
                .seller(personRepository.findById(c.getSellerId()).orElseThrow(() -> new ResourceNotFoundException("Seller not found")))
                .buyer( personRepository.findById(c.getBuyerId() ).orElseThrow(() -> new ResourceNotFoundException("Buyer not found")))
                .branch(branchRepository.findById(c.getBranchId()).orElseThrow(() -> new ResourceNotFoundException("Branch not found")))
                .saleType(c.getSaleType())
                .totalAmount(c.getTotalAmount())
                .amountPaid(c.getAmountPaid())
                .installmentAmount(c.getInstallmentAmount())
                .paymentMethod(c.getPaymentMethod())
                .paymentStatus(c.getPaymentStatus())
                .createdBy(c.getCreatedBy())
                .daysAmountBetweenInstallments(c.getDaysAmountBetweenInstallments())
                .build();

        Contract savedContract = contractRepository.save(contract);

        dto.getContractInstallments().forEach(i -> i.setContract(savedContract));
        installmentRepository.saveAll(dto.getContractInstallments());

        List<ContractImage> images = dto.getContractImage().stream()
                .map(imgDto -> {

                    String imageKey = imageStorageService.upload(imgDto.getImage());

                    return ContractImage.builder()
                            .image(imageKey)
                            .deleted(imgDto.isDeleted())
                            .contract(savedContract)
                            .build();
                })
                .collect(Collectors.toList());

        contractImageRepository.saveAll(images);

        return savedContract;
    }



    public Page<ContractResponseDTO> getAllContracts(String contractNumber,
                                                     LocalDate contractDate,
                                                     int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("contractDate").descending());

        Page<Contract> contracts = contractRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (contractNumber != null && !contractNumber.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("contractNumber")),
                        "%" + contractNumber.toLowerCase() + "%"));
            }
            if (contractDate != null) {
                predicates.add(cb.equal(root.get("contractDate"), contractDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return contracts.map(this::toDto);
    }

    public ContractResponseDTO getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));

        if (contract.isDeleted()) {
            throw new ResourceNotFoundException("Contract is deleted");
        }
        return toDto(contract);
    }


    @Transactional
    public ContractResponseDTO updateContract(Long id, ContractRequestDTO dto) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));

        if (contract.isDeleted()) throw new IllegalStateException("Cannot update a deleted contract");

        contract.setCar(carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found")));
        contract.setSeller(personRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found")));
        contract.setBuyer(personRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found")));
        contract.setBranch(branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found")));

        contract.setContractNumber(dto.getContractNumber());
        contract.setContractDate(dto.getContractDate());
        contract.setSaleType(dto.getSaleType());
        contract.setPaymentMethod(dto.getPaymentMethod());
        contract.setPaymentStatus(dto.getPaymentStatus());
        contract.setUpdatedAt(new Date());

        Contract saved = contractRepository.save(contract);
        return toDto(saved);
    }


    @Transactional
    public void softDeleteContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));
        contract.setDeleted(true);
        contract.setUpdatedAt(new Date());
        contractRepository.save(contract);
    }


    private ContractResponseDTO toDto(Contract c) {
        return ContractResponseDTO.builder()
                .id(c.getId())
                .contractNumber(c.getContractNumber())
                .contractDate(c.getContractDate())

                .carName(c.getCar().getModel())
                .sellerUsername(c.getSeller().getUsername())
                .buyerUsername(c.getBuyer().getUsername())
                .branchName(c.getBranch() != null ? c.getBranch().getName() : null)

                .installmentAmount(c.getInstallmentAmount())
                .daysAmountBetweenInstallments(c.getDaysAmountBetweenInstallments())
                .saleType(c.getSaleType())
                .totalAmount(c.getTotalAmount())
                .amountPaid(c.getAmountPaid())
                .paymentMethod(c.getPaymentMethod())
                .paymentStatus(c.getPaymentStatus())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .deleted(c.isDeleted())
                .build();
    }
}
