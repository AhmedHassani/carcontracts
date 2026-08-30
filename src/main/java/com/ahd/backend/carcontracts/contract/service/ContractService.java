package com.ahd.backend.carcontracts.contract.service;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.contract.dto.*;
import com.ahd.backend.carcontracts.contract.mapper.ContractMapper;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;  // ✅ ADD THIS IMPORT
import com.ahd.backend.carcontracts.notification.service.NotificationSender;
import com.ahd.backend.carcontracts.notification.service.MessageService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {
    private final ContractsRepository contractRepo;
    private final PersonRepository personRepo;
    private final CarRepository carRepo;
    private final PaymentPlanRepository planRepo;
    private final Helper helper;
    private final ContractNumberGeneratorService contractNumberGenerator;
    private final NotificationSender notificationSender;
    private final MessageService messageService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Auditable(operation = "اضافة عقد", captureArgs = true, captureResult = true)
    public ContractResponse addContract(ContractRequest request) {
        Person seller = personRepo.getReferenceById(request.getSellerId());
        Person buyer = personRepo.getReferenceById(request.getBuyerId());
        Car car = carRepo.getReferenceById(request.getCarId());
        
        if (Objects.equals(seller.getId(), buyer.getId())) {
            throw new IllegalArgumentException("you can't make the buyer and the seller be the same person");
        }

        PaymentPlan paymentPlan = planRepo.getReferenceById(request.getPaymentId());
        
        if (paymentPlan.getPaymentType() == PaymentType.CASH) {
            car.setStatus("Paid");
            car.setPaidAt(LocalDateTime.now());
            paymentPlan.setStatus(PaymentStatus.COMPLETED);
            paymentPlan.setRemainingAmount(BigDecimal.ZERO);
            paymentPlan.setDownPayment(paymentPlan.getTotalAmount());
        } else {
            car.setStatus("Active");
            car.setPaidAt(LocalDateTime.now());
            paymentPlan.setStatus(PaymentStatus.ACTIVE);
        }
        
        Person guarantor = null;
        if (request.getGuarantorId() != null) {
            guarantor = personRepo.getReferenceById(request.getGuarantorId());
        }
        
        Person possessor = null;
        if (request.getPossessorId() != null) {
            possessor = personRepo.getReferenceById(request.getPossessorId());
        } else {
            possessor = buyer;
        }

        Long companyId = getCompanyId();
        String contractNumber = contractNumberGenerator.generateContractNumber(companyId);
        
        Contracts contract = new Contracts();
        contract.setContractDate(request.getContractDate());
        contract.setSeller(seller);
        contract.setBuyer(buyer);
        contract.setGuarantor(guarantor);
        contract.setPossessor(possessor);
        contract.setCar(car);
        contract.setOnus(request.isOnus()); 
        contract.setPaymentPlan(paymentPlan);
        contract.setCompanyId(getCompanyId());
        contract.setContractNumber(contractNumber); 
        contract.setDescription(request.getDescription());
        contract = contractRepo.saveAndFlush(contract);
        
        // ✅ Add notification for contract creation
        NotificationContext context = notificationSender.createContractContext("CREATE", contract);
        notificationSender.notifyContractOperation(context);
        
        return ContractMapper.toDetails(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getAllContract(ContractSearchCriteria criteria, Pageable pageable) {
        Specification<Contracts> spec = ContractSpecification.buildSpecification(criteria);
        
        ContractSearchCriteria enrichedCriteria = ContractSearchCriteria.builder()
                .keyword(criteria.keyword())
                .sortBy(criteria.sortBy())
                .sortDirection(criteria.sortDirection())
                .carType(criteria.carType())
                .carNumber(criteria.carNumber())
                .StatusPaymant(criteria.StatusPaymant())
                .BuyerName(criteria.BuyerName())
                .BuyerPhone(criteria.BuyerPhone())
                .SellerName(criteria.SellerName())
                .SellerPhone(criteria.SellerPhone())
                .possessorName(criteria.possessorName())
                .possessorPhone(criteria.possessorPhone())
                .onus(criteria.onus())  
                .name(criteria.name())
                .companyId(getCompanyId())
                .id(criteria.id())
                .chassisNumber(criteria.chassisNumber())
                .status(criteria.status())
                .contractNumber(criteria.contractNumber())
                .build();

        spec = ContractSpecification.buildSpecification(enrichedCriteria);
        Page<Contracts> contracts = contractRepo.findAll(spec, pageable);
        return contracts.map(ContractMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public Page<ContractPaymentsResponse> getAllContractPayments(ContractSearchCriteria criteria, Pageable pageable) {
        Specification<Contracts> spec = ContractSpecification.buildSpecification(criteria);
        
        ContractSearchCriteria enrichedCriteria = ContractSearchCriteria.builder()
                .keyword(criteria.keyword())
                .sortBy(criteria.sortBy())
                .sortDirection(criteria.sortDirection())
                .carType(criteria.carType())
                .carNumber(criteria.carNumber())
                .StatusPaymant(criteria.StatusPaymant())
                .BuyerName(criteria.BuyerName())
                .BuyerPhone(criteria.BuyerPhone())
                .SellerName(criteria.SellerName())
                .SellerPhone(criteria.SellerPhone())
                .possessorName(criteria.possessorName())
                .possessorPhone(criteria.possessorPhone())
                .companyId(getCompanyId())
                .contractNumber(criteria.contractNumber())
                .name(criteria.name())
                .id(criteria.id())
                .chassisNumber(criteria.chassisNumber())
                .status(criteria.status())
                .build();

        spec = ContractSpecification.buildSpecification(enrichedCriteria);
        return contractRepo.findAll(spec, pageable)
                .map(ContractMapper::toPayments);
    }

    @Transactional
    @Auditable(operation = "حذف عقد", captureArgs = true, captureResult = true)
    public void softDeleteContract(Long contractId) {
        Contracts contract = contractRepo.findByIdAndCompanyId(contractId, getCompanyId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));

        Contracts contractToDelete = copyContract(contract);

        if (contract.getPaymentPlan() != null) {
            contract.getPaymentPlan().setDeleted(true);
            contract.getPaymentPlan().getInstallments()
                    .forEach(i -> i.setDeleted(true));
        }
        contract.setDeleted(true);
        
        contractRepo.save(contract);
        
        // ✅ Add notification for contract deletion
        NotificationContext context = notificationSender.createContractContext("DELETE", contractToDelete);
        notificationSender.notifyContractOperation(context);
    }
   
    @Transactional
    @Auditable(operation = "تغير كتاب العقد", captureArgs = true, captureResult = true)
    public void updateContracttemplateId(Long contractId, Long templateId) {  // ✅ Fixed method name
        Contracts contract = contractRepo.findByIdAndCompanyId(contractId, getCompanyId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));
        contract.setTemplateId(templateId);  // ✅ Fixed variable name
        contractRepo.save(contract);
    }
    
    // ✅ ADD THIS METHOD - Update Contract with notification
    // @Transactional
    // @Auditable(operation = "تحديث عقد", captureArgs = true, captureResult = true)
    // public ContractResponse updateContract(Long contractId, ContractUpdateRequest request) {
    //     Contracts existingContract = contractRepo.findByIdAndCompanyId(contractId, getCompanyId())
    //             .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));
        
    //     // Store old contract for change tracking
    //     Contracts oldContract = copyContract(existingContract);
        
    //     // Update fields
    //     if (request.getContractDate() != null) {
    //         existingContract.setContractDate(request.getContractDate());
    //     }
    //     if (request.getOnus() != null) {
    //         existingContract.setOnus(request.getOnus());
    //     }
    //     if (request.getSellerId() != null) {
    //         existingContract.setSeller(personRepo.getReferenceById(request.getSellerId()));
    //     }
    //     if (request.getBuyerId() != null) {
    //         existingContract.setBuyer(personRepo.getReferenceById(request.getBuyerId()));
    //     }
    //     if (request.getGuarantorId() != null) {
    //         existingContract.setGuarantor(personRepo.getReferenceById(request.getGuarantorId()));
    //     }
    //     if (request.getPossessorId() != null) {
    //         existingContract.setPossessor(personRepo.getReferenceById(request.getPossessorId()));
    //     }
        
    //     Contracts updatedContract = contractRepo.save(existingContract);
        
    //     // Generate change details and send notification
    //     String changeDetails = notificationSender.generateContractChangeDetails(oldContract, updatedContract);
    //     NotificationContext context = notificationSender.createContractContext("UPDATE", updatedContract, changeDetails);
    //     notificationSender.notifyContractOperation(context);
        
    //     return ContractMapper.toDetails(updatedContract);
    // }

    // ✅ ADD THIS HELPER METHOD - Copy contract for change tracking
    private Contracts copyContract(Contracts contract) {
        if (contract == null) return null;
        
        Contracts copy = new Contracts();
        copy.setId(contract.getId());
        copy.setContractNumber(contract.getContractNumber());
        copy.setContractDate(contract.getContractDate());
        copy.setOnus(contract.isOnus());
        copy.setCompanyId(contract.getCompanyId());
        copy.setTemplateId(contract.getTemplateId());
        
        // Copy related entities (just IDs for tracking)
        if (contract.getSeller() != null) {
            copy.setSeller(contract.getSeller());
        }
        if (contract.getBuyer() != null) {
            copy.setBuyer(contract.getBuyer());
        }
        if (contract.getGuarantor() != null) {
            copy.setGuarantor(contract.getGuarantor());
        }
        if (contract.getPossessor() != null) {
            copy.setPossessor(contract.getPossessor());
        }
        if (contract.getCar() != null) {
            copy.setCar(contract.getCar());
        }
        if (contract.getPaymentPlan() != null) {
            copy.setPaymentPlan(contract.getPaymentPlan());
        }
        
        return copy;
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
}