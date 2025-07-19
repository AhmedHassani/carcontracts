package com.ahd.backend.carcontracts.contract.service;


import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.contract.dto.ContractRequest;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.contract.mapper.ContractMapper;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractsRepository contractRepo;
    private final PersonRepository personRepo;
    private final CarRepository carRepo;
    private final PaymentPlanRepository planRepo;


    @Transactional
    public ContractResponse addContract(ContractRequest request) {
        Person seller = personRepo.getReferenceById(request.getSellerId());
        Person buyer  = personRepo.getReferenceById(request.getBuyerId());
        Car car    = carRepo.getReferenceById(request.getCarId());
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
        contract = contractRepo.save(contract);
        return ContractMapper.toDetails(contract);
    }

}
