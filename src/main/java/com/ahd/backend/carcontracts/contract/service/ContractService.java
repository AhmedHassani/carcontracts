package com.ahd.backend.carcontracts.contract.service;


import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.contract.dto.*;
import com.ahd.backend.carcontracts.contract.mapper.ContractMapper;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
//import com.ahd.backend.carcontracts.notification.model.AppNotification;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import com.ahd.backend.carcontracts.person.dto.PersonSearchCriteria;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.person.service.PersonSpecification;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
//import com.ahd.backend.carcontracts.notification.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {
    private final ContractsRepository contractRepo;
    private final PersonRepository personRepo;
    private final CarRepository carRepo;
    private final PaymentPlanRepository planRepo;
    //private final NotificationService notificationService;
    private final Helper helper;



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Auditable(operation = "اضافة عقد", captureArgs = true, captureResult = true)
    //notification
    //contract.getId(); قام المستخدم  ;helper.getCurrentUser.userName بانشاء العقد رقم
    //to all company
    public ContractResponse addContract(ContractRequest request) {
        Person seller = personRepo.getReferenceById(request.getSellerId());
        Person buyer  = personRepo.getReferenceById(request.getBuyerId());
        Car car    = carRepo.getReferenceById(request.getCarId());
        if ( Objects.equals(seller.getId(), buyer.getId())) {
            throw new IllegalArgumentException("you can't make the buyer and the seller be the same person");
        }

        PaymentPlan paymentPlan = planRepo.getReferenceById(request.getPaymentId());
        if(paymentPlan.getPaymentType() == PaymentType.CASH){
            car.setStatus("Paid");
            car.setPaidAt(LocalDateTime.now());
            paymentPlan.setStatus(PaymentStatus.COMPLETED);
            paymentPlan.setRemainingAmount(BigDecimal.ZERO);
            paymentPlan.setDownPayment(paymentPlan.getTotalAmount());
        }else{
            car.setStatus("Active");
            car.setPaidAt(LocalDateTime.now());
            paymentPlan.setStatus(PaymentStatus.ACTIVE);
        }
        Person guarantor = null;
        if (request.getGuarantorId() != null) {
            guarantor = personRepo.getReferenceById(request.getGuarantorId());
        }
        Contracts contract = new Contracts();
        contract.setContractDate(request.getContractDate());
        contract.setSeller(seller);
        contract.setBuyer(buyer);
        contract.setGuarantor(guarantor);
        contract.setCar(car);
        contract.setPaymentPlan(paymentPlan);
        contract.setCompanyId(getCompanyId());
        contract = contractRepo.saveAndFlush(contract);
//        AppNotification notif = new AppNotification();
//        notif.setTitle("اضافة عقد");
//        notif.setBody("تم اضافة العقد رقم" + contract.getId() + " بنجاح");
//        notif.setNotificationDate(LocalDateTime.now());
//        notif.setPermisson("CompanyUsers");
//        notificationService.insertNotificationAsync(notif);
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
                .name(criteria.name())
                .companyId(getCompanyId())
                .id(criteria.id())
//                .buyerNationalId(criteria.buyerNationalId())
//                .sellerNationalId(criteria.sellerNationalId())
                .chassisNumber(criteria.chassisNumber())
                .status(criteria.status())
                .build();

        spec = ContractSpecification.buildSpecification(enrichedCriteria);
        Page<Contracts> contracts = contractRepo.findAll(spec, pageable);
        return contracts.map(ContractMapper::toDetails);
    }


    @Transactional(readOnly = true)
    public  Page<ContractPaymentsResponse>  getAllContractPayments(ContractSearchCriteria criteria, Pageable pageable) {
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
                .companyId(getCompanyId())
                .name(criteria.name())
                .id(criteria.id())
//                .buyerNationalId(criteria.buyerNationalId())
//                .sellerNationalId(criteria.sellerNationalId())
                .chassisNumber(criteria.chassisNumber())
                .status(criteria.status())
                .build();

        spec = ContractSpecification.buildSpecification(enrichedCriteria);
        return contractRepo.findAll(spec, pageable)
                .map(ContractMapper::toPayments);
    }

    @Transactional
    @Auditable(operation = "حذف عقد", captureArgs = true, captureResult = true)
    //notification
    //contractId قام المستخدم  ;helper.getCurrentUser.userName بحذف العقد رقم
    //to all company
    public void softDeleteContract(Long contractId) {
        Contracts contract = contractRepo.findByIdAndCompanyId(contractId , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));

        if (contract.getPaymentPlan() != null) {
            contract.getPaymentPlan().setDeleted(true);

            contract.getPaymentPlan().getInstallments()
                    .forEach(i -> i.setDeleted(true));
        }
        contract.setDeleted(true);
        // notificationService.sendNotificationToDevice(
        //         "حذف عقد",
        //         "تم حذف العقد رقم" + contract.getId() + " بنجاح "
        // );
//        AppNotification notif = new AppNotification();
//        notif.setTitle("حذف عقد");
//        notif.setBody("تم حذف العقد رقم " + contract.getId() + " بنجاح");
//        notif.setNotificationDate(LocalDateTime.now());
//        //    notif.setCompany(savedCompany);
//        notif.setPermisson("CompanyUsers");
//        notificationService.insertNotificationAsync(notif);

        contractRepo.save(contract);
    }


    @Transactional
    @Auditable(operation = "تحديث عقد", captureArgs = true, captureResult = true)
    //notification
    //contractId قام المستخدم  ;helper.getCurrentUser.userName تحديث العقد رقم
    //to all company
    public void updateContracttemplateId(Long contractId , Long templateId) {
        Contracts contract = contractRepo.findByIdAndCompanyId(contractId , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));
        contract.setTemplateId(templateId);
        contractRepo.save(contract);
    }

    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }
}
