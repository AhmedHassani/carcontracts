package com.ahd.backend.carcontracts.contract.service;


import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.contract.dto.*;
import com.ahd.backend.carcontracts.contract.mapper.ContractMapper;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.notification.model.AppNotification;
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
import com.ahd.backend.carcontracts.notification.service.NotificationService;

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
    private final NotificationService notificationService;
    private final Helper helper;



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Auditable(operation = "CREATE_CONTRACT", captureArgs = true, captureResult = true)
    public ContractResponse addContract(ContractRequest request) {
        Person seller = personRepo.getReferenceById(request.getSellerId());
        Person buyer  = personRepo.getReferenceById(request.getBuyerId());
        Car car    = carRepo.getReferenceById(request.getCarId());
        car.setStatus("Active");
        PaymentPlan paymentPlan = planRepo.getReferenceById(request.getPaymentId());
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
        notificationService.sendNotificationToDevice(
                "اضافة عقد",
                "تم اضافة العقد رقم" + contract.getId() + " بنجاح "
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("اضافة عقد");
        notif.setBody("تم اضافة العقد رقم" + contract.getId() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
        //    notif.setCompany(savedCompany);
        notif.setPermisson("CompanyUsers");
        notificationService.insertNotificationAsync(notif);
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
                .companyId(getCompanyId())
                .build();

        spec = ContractSpecification.buildSpecification(enrichedCriteria);
        Page<Contracts> contracts = contractRepo.findAll(spec, pageable);
        return contracts.map(ContractMapper::toDetails);
    }


    @Transactional(readOnly = true)
    public  List<ContractPaymentsResponse>  getAllContractPayments(ContractPaymentsSearchCriteria criteria, Pageable pageable) {
        Specification<Contracts> spec = ContractPaymentsSpecification.buildSpecification(criteria);
        ContractPaymentsSearchCriteria enrichedCriteria = ContractPaymentsSearchCriteria.builder()
                .keyword(criteria.keyword())
                .sortBy(criteria.sortBy())
                .sortDirection(criteria.sortDirection())
                .startDate(criteria.startDate())
                .endDate(criteria.endDate())
                .status(criteria.status())
                .companyId(getCompanyId())
                .build();

        spec = ContractPaymentsSpecification.buildSpecification(enrichedCriteria);
        Page<Contracts> contracts = contractRepo.findAll(spec, pageable);
        return contracts.getContent().stream()
                .map(ContractMapper::toPayments)
                .toList();
    }

    @Transactional
    @Auditable(operation = "DELETE_CONTRACT", captureArgs = true, captureResult = true)
    public void softDeleteContract(Long contractId) {
        Contracts contract = contractRepo.findByIdAndCompanyId(contractId , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + contractId));

        if (contract.getPaymentPlan() != null) {
            contract.getPaymentPlan().setDeleted(true);

            contract.getPaymentPlan().getInstallments()
                    .forEach(i -> i.setDeleted(true));
        }
        contract.setDeleted(true);
        notificationService.sendNotificationToDevice(
                "حذف عقد",
                "تم حذف العقد رقم" + contract.getId() + " بنجاح "
        );
        AppNotification notif = new AppNotification();
        notif.setTitle("حذف عقد");
        notif.setBody("تم حذف العقد رقم " + contract.getId() + " بنجاح");
        notif.setNotificationDate(LocalDateTime.now());
    //    notif.setCompany(savedCompany);
        notif.setPermisson("CompanyUsers");
        notificationService.insertNotificationAsync(notif);

        contractRepo.save(contract);
    }

    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }
}
