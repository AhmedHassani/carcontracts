package com.ahd.backend.carcontracts.authorization.service;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationResponse;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpsertRequest;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpdateRequest;
import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import com.ahd.backend.carcontracts.authorization.mapper.AuthorizationMapper;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import com.ahd.backend.carcontracts.authorization.repository.AuthorizationRepository;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.util.Helper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;  // ADD THIS IMPORT

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final PersonRepository personRepository;
    private final CarRepository carRepository;
    private final Helper helper;
    private final AuthorizationHistoryService historyService;  // ADD THIS LINE

    @Transactional(readOnly = true)
    public Page<AuthorizationResponse> list(Pageable pageable, AuthorizationSearchCriteria searchCriteria) {

        AuthorizationSearchCriteria criteriaWithCompany = new AuthorizationSearchCriteria(
                searchCriteria.keyword(),
                searchCriteria.sortBy(),
                searchCriteria.sortDirection(),
                searchCriteria.authorizationNumber(),
                searchCriteria.companyAgent(),
                searchCriteria.authorizationDateStart(),
                searchCriteria.authorizationDateEnd(),
                getCompanyId()
        );

        return authorizationRepository.findAll(
                AuthorizationSpecification.buildSpecification(criteriaWithCompany),
                pageable
        ).map(AuthorizationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AuthorizationResponse get(Long id) {
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        return AuthorizationMapper.toResponse(entity);
    }
    
    @Auditable(operation = "انشاء تخويل", captureArgs = true, captureResult = true)
    public AuthorizationResponse create(AuthorizationUpsertRequest r) {
        if (authorizationRepository.existsByAuthorizationNumber(r.getAuthorizationNumber())) {
            throw new IllegalArgumentException("authorizationNumber already exists");
        }
        Person buyer = personRepository.findById(r.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found: " + r.getBuyerId()));
        Car car = carRepository.findById(r.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + r.getCarId()));
        Authorization entity = AuthorizationMapper.fromUpsertRequest(r, buyer, car);
        entity.setCompanyId(getCompanyId());
        return AuthorizationMapper.toResponse(authorizationRepository.save(entity));
    }
    
    @Auditable(operation = "تحديث تخويل", captureArgs = true, captureResult = true)
    public AuthorizationResponse update(Long id, AuthorizationUpsertRequest r) {
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        if (!entity.getAuthorizationNumber().equals(r.getAuthorizationNumber())
                && authorizationRepository.existsByAuthorizationNumber(r.getAuthorizationNumber())) {
            throw new IllegalArgumentException("authorizationNumber already exists");
        }
        Person buyer = personRepository.findById(r.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found: " + r.getBuyerId()));
        Car car = carRepository.findById(r.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + r.getCarId()));
        AuthorizationMapper.update(entity, r, buyer, car);
        return AuthorizationMapper.toResponse(authorizationRepository.save(entity));
    }
    
    @Auditable(operation = "حذف تخويل", captureArgs = true, captureResult = true)
    public void delete(Long id) {
        if (!authorizationRepository.existsByIdAndCompanyId(id , getCompanyId())) {
            throw new EntityNotFoundException("Authorization not found: " + id);
        }
        authorizationRepository.deleteById(id);
    }
    
    @Auditable(operation = "تحديث تخويل", captureArgs = true, captureResult = true)
    public AuthorizationResponse updatetemplateId(Long id, Long templateId) {
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        entity.setTemplateId(templateId);
        return AuthorizationMapper.toResponse(authorizationRepository.save(entity));
    }

    @Auditable(operation = "تحديث التخويل الى مشتري جديد", captureArgs = true, captureResult = true)
    public AuthorizationResponse updateIsChange(AuthorizationUpdateRequest r) {
        Long id = r.getAuthorizationId();
        
        // Get the existing authorization
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        
        // Get old buyer ID before update
        Long oldBuyerId = entity.getBuyer() != null ? entity.getBuyer().getId() : null;
        
        // Get new buyer
        Person newBuyer = personRepository.findById(r.getNewBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("New buyer not found: " + r.getNewBuyerId()));
        
        // Set the user ID who made the change
        r.setUserId(helper.getCurrentUserId());
        
        // Update the buyer and set is_change flag to true
        entity.setBuyer(newBuyer);
        entity.setChange(true);  // This is the is_change field in Authorization table
        
        // Save the updated authorization
        Authorization savedEntity = authorizationRepository.save(entity);
        
        // Create history entry with old and new buyer
        historyService.createHistoryEntry(
            savedEntity, 
            r.getNewBuyerId(), 
            oldBuyerId, 
            r.getUserId()
        );
        
        return AuthorizationMapper.toResponse(savedEntity);
    }

    // Add this method to AuthorizationService
    @Transactional(readOnly = true)
    public List<AuthorizationHistory> getChangeHistory(Long authorizationId) {
        // Verify authorization exists and belongs to company
        authorizationRepository.findByIdAndCompanyId(authorizationId, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + authorizationId));
        
        return historyService.getHistoryByAuthorizationId(authorizationId);
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
}