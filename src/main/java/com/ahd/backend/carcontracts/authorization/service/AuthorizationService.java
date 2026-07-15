package com.ahd.backend.carcontracts.authorization.service;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationResponse;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpsertRequest;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpdateRequest;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationHistoryResponse;
import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import com.ahd.backend.carcontracts.authorization.mapper.AuthorizationMapper;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import com.ahd.backend.carcontracts.authorization.repository.AuthorizationRepository;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.util.Helper;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;
import com.ahd.backend.carcontracts.notification.service.NotificationSender;
import com.ahd.backend.carcontracts.notification.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final PersonRepository personRepository;
    private final CarRepository carRepository;
    private final Helper helper;
    private final AuthorizationHistoryService historyService;
    private final NotificationSender notificationSender;
    private final MessageService messageService;

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
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        return AuthorizationMapper.toResponse(entity);
    }
    
    @Auditable(operation = "انشاء تخويل", captureArgs = true, captureResult = true)
    public AuthorizationResponse create(AuthorizationUpsertRequest r) {
        if (authorizationRepository.existsByAuthorizationNumberAndCompanyId(r.getAuthorizationNumber(), getCompanyId())) {
            throw new IllegalArgumentException("authorizationNumber already exists");
        }
        Long maxAuthorizationNumber = authorizationRepository.findMaxAuthorizationNumberByCompanyId(getCompanyId());
        if (r.getAuthorizationNumber() == null) {
            r.setAuthorizationNumber(maxAuthorizationNumber != null ? maxAuthorizationNumber + 1 : 1);
        }
        Person buyer = personRepository.findById(r.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found: " + r.getBuyerId()));
        Car car = carRepository.findById(r.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + r.getCarId()));
        Authorization entity = AuthorizationMapper.fromUpsertRequest(r, buyer, car);
        entity.setCompanyId(getCompanyId());
        Authorization savedEntity = authorizationRepository.save(entity);
        
        // ✅ Add notification for authorization creation
        NotificationContext context = notificationSender.createAuthorizationContext("CREATE", savedEntity);
        notificationSender.notifyAuthorizationOperation(context);
        
        return AuthorizationMapper.toResponse(savedEntity);
    }
    
    @Auditable(operation = "تحديث تخويل", captureArgs = true, captureResult = true)
    public AuthorizationResponse update(Long id, AuthorizationUpsertRequest r) {
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        
        // Store old authorization for change tracking
        Authorization oldAuthorization = copyAuthorization(entity);
        
        if (!entity.getAuthorizationNumber().equals(r.getAuthorizationNumber())
                && authorizationRepository.existsByAuthorizationNumberAndCompanyId(r.getAuthorizationNumber(), getCompanyId())) {
            throw new IllegalArgumentException("authorizationNumber already exists");
        }
        Person buyer = personRepository.findById(r.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found: " + r.getBuyerId()));
        Car car = carRepository.findById(r.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + r.getCarId()));
        AuthorizationMapper.update(entity, r, buyer, car);
        Authorization savedEntity = authorizationRepository.save(entity);
        
        // Generate change details and send notification
        String changeDetails = notificationSender.generateAuthorizationChangeDetails(oldAuthorization, savedEntity);
        NotificationContext context = notificationSender.createAuthorizationContext("UPDATE", savedEntity, changeDetails);
        notificationSender.notifyAuthorizationOperation(context);
        
        return AuthorizationMapper.toResponse(savedEntity);
    }
    
    @Auditable(operation = "حذف تخويل", captureArgs = true, captureResult = true)
    public void delete(Long id) {
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        
        // Store authorization info before deletion for notification
        Authorization authorizationToDelete = copyAuthorization(entity);
        
        authorizationRepository.deleteById(id);
        
        // ✅ Add notification for authorization deletion
        NotificationContext context = notificationSender.createAuthorizationContext("DELETE", authorizationToDelete);
        notificationSender.notifyAuthorizationOperation(context);
    }
    
    @Auditable(operation = "تحديث تخويل", captureArgs = true, captureResult = true)
    public AuthorizationResponse updatetemplateId(Long id, Long templateId) {
        Authorization entity = authorizationRepository.findByIdAndCompanyId(id, getCompanyId())
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
    
    // Get old buyer name
    String oldBuyerName = entity.getBuyer() != null ? getPersonName(entity.getBuyer()) : "غير محدد";
    
    // Get new buyer
    Person newBuyer = personRepository.findById(r.getNewBuyerId())
            .orElseThrow(() -> new EntityNotFoundException("New buyer not found: " + r.getNewBuyerId()));
    String newBuyerName = getPersonName(newBuyer);
    
    // Save old buyer ID for history
    Long oldBuyerId = entity.getBuyer() != null ? entity.getBuyer().getId() : null;
    
    // Update the buyer
    entity.setBuyer(newBuyer);
    entity.setChange(true);
    
    // Save the updated authorization
    Authorization savedEntity = authorizationRepository.save(entity);
    
    // Create history entry
    historyService.createHistoryEntry(savedEntity, r.getNewBuyerId(), oldBuyerId);
    
    // Create change details message
    String changeDetails = String.format("• تم تغيير المشتري من '%s' إلى '%s'", oldBuyerName, newBuyerName);
    
    // Send notification
    NotificationContext context = notificationSender.createAuthorizationContext("BUYER_CHANGE", savedEntity, changeDetails);
    notificationSender.notifyAuthorizationOperation(context);
    
    return AuthorizationMapper.toResponse(savedEntity);
}

   
@Transactional(readOnly = true)
public List<AuthorizationHistoryResponse> getChangeHistory(Long authorizationId) {
    // Verify authorization exists and belongs to company
    authorizationRepository.findByIdAndCompanyId(authorizationId, getCompanyId())
            .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + authorizationId));
    
    return historyService.getHistoryResponseByAuthorizationId(authorizationId);
}

    // ✅ ADD THIS HELPER METHOD - Copy authorization for change tracking
    private Authorization copyAuthorization(Authorization authorization) {
        if (authorization == null) return null;
        
        Authorization copy = new Authorization();
        copy.setId(authorization.getId());
        copy.setAuthorizationNumber(authorization.getAuthorizationNumber());
        copy.setAuthorizationDate(authorization.getAuthorizationDate());
        copy.setCompanyAgent(authorization.getCompanyAgent());
        copy.setChange(authorization.isChange());
        copy.setCompanyId(authorization.getCompanyId());
        copy.setTemplateId(authorization.getTemplateId());
        
        // Copy related entities
        if (authorization.getBuyer() != null) {
            copy.setBuyer(authorization.getBuyer());
        }
        if (authorization.getCar() != null) {
            copy.setCar(authorization.getCar());
        }
        
        return copy;
    }
    
    // ✅ ADD THIS HELPER METHOD - Get person name
    private String getPersonName(Person person) {
        if (person == null) return "غير محدد";
        StringBuilder fullName = new StringBuilder();
        if (person.getFirstName() != null) fullName.append(person.getFirstName());
        if (person.getFatherName() != null) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getFatherName());
        }
        if (person.getGrandfatherName() != null) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getGrandfatherName());
        }
        if (person.getSurname() != null) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getSurname());
        }
        return fullName.length() > 0 ? fullName.toString() : "بدون اسم";
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
}