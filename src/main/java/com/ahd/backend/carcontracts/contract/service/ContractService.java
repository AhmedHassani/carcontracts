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
import com.ahd.backend.carcontracts.payment.model.Installment;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    //private final NotificationService notificationService;  // Uncomment when needed
    private final Helper helper;

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
        
        // Handle guarantor (can be null)
        Person guarantor = null;
        if (request.getGuarantorId() != null) {
            guarantor = personRepo.getReferenceById(request.getGuarantorId());
        }
        
        // ADD THIS: Handle possessor (defaults to buyer if not provided)
        Person possessor = null;
        if (request.getPossessorId() != null) {
            possessor = personRepo.getReferenceById(request.getPossessorId());
        } else {
            possessor = buyer;
        }
        
        Contracts contract = new Contracts();
        contract.setContractDate(request.getContractDate());
        contract.setSeller(seller);
        contract.setBuyer(buyer);
        contract.setGuarantor(guarantor);
        contract.setPossessor(possessor);  // ADD THIS LINE
        contract.setCar(car);
        contract.setOnus(request.isOnus()); 
        contract.setPaymentPlan(paymentPlan);
        contract.setCompanyId(getCompanyId());
        contract = contractRepo.saveAndFlush(contract);
        
        // Notification commented out for now
        // AppNotification notif = new AppNotification();
        // notif.setTitle("اضافة عقد");
        // notif.setBody("تم اضافة العقد رقم" + contract.getId() + " بنجاح");
        // notif.setNotificationDate(LocalDateTime.now());
        // notif.setPermisson("CompanyUsers");
        // notificationService.insertNotificationAsync(notif);
        
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
                .possessorName(criteria.possessorName())  // ADD THIS
                .possessorPhone(criteria.possessorPhone())  // ADD THIS
                .onus(criteria.onus())  
                .name(criteria.name())
                .companyId(getCompanyId())
                .possessorName(criteria.possessorName())
                .possessorPhone(criteria.possessorPhone())
                .id(criteria.id())
                .chassisNumber(criteria.chassisNumber())
                .status(criteria.status())
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
                .possessorName(criteria.possessorName())  // ADD THIS
                .possessorPhone(criteria.possessorPhone())  // ADD THIS
                .companyId(getCompanyId())
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

        if (contract.getPaymentPlan() != null) {
            contract.getPaymentPlan().setDeleted(true);
            contract.getPaymentPlan().getInstallments()
                    .forEach(i -> i.setDeleted(true));
        }
        contract.setDeleted(true);
        
        // Uncomment when notification service is available
        // notificationService.sendNotificationToDevice("حذف عقد", "تم حذف العقد رقم" + contract.getId() + " بنجاح");
        // AppNotification notif = new AppNotification();
        // notif.setTitle("حذف عقد");
        // notif.setBody("تم حذف العقد رقم " + contract.getId() + " بنجاح");
        // notif.setNotificationDate(LocalDateTime.now());
        // notif.setPermisson("CompanyUsers");
        // notificationService.insertNotificationAsync(notif);

        contractRepo.save(contract);
    }

    @Transactional
    @Auditable(operation = "تحديث عقد", captureArgs = true, captureResult = true)
    public void updateContracttemplateId(Long contractId, Long templateId) {
        Contracts contract = contractRepo.findByIdAndCompanyId(contractId, getCompanyId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));
        contract.setTemplateId(templateId);
        contractRepo.save(contract);
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
}