package com.ahd.backend.carcontracts.notification.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSender {
    
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final Helper helper;
    private final MessageService messageService;

    // ==================== CAR NOTIFICATIONS ====================
    
    @Transactional
    public void notifyCarOperation(NotificationContext context) {
        try {
            AppUser currentUser = helper.getCurrentUser();
            Long companyId = helper.getCurrentCompanyId();
            
            if (currentUser == null || companyId == null) {
                log.warn(messageService.getMessage("notification.car.error.no.user"));
                return;
            }
            
            List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
            
            if (companyUsers.isEmpty()) {
                log.info(messageService.getMessage("notification.car.error.no.token", companyId));
                return;
            }
            
            log.info(messageService.getMessage("notification.car.log.sending", 
                context.getOperation(), companyUsers.size(), companyId));
            
            NotificationRequest request = NotificationRequest.builder()
                .title(context.getTitle())
                .message(context.getMessage())
                .actionBy(currentUser.getEmail())
                .actionType(context.getActionType())
                .actionDate(LocalDateTime.now())
                .companyId(companyId)
                .targetUserIds(companyUsers.stream()
                    .map(AppUser::getId)
                    .collect(Collectors.toList()))
                .build();
            
            Map<String, String> additionalData = new HashMap<>();
            if (context.getAdditionalData() != null) {
                context.getAdditionalData().forEach((k, v) -> 
                    additionalData.put(k, String.valueOf(v)));
            }
            request.setAdditionalData(additionalData);
            
            notificationService.sendNotification(request);
            
            log.info(messageService.getMessage("notification.car.log.success", 
                context.getOperation(), context.getEntityId()));
            
        } catch (Exception e) {
            log.error(messageService.getMessage("notification.car.log.failed", 
                context.getOperation(), context.getEntityId(), e.getMessage()));
        }
    }
    
    public NotificationContext createCarContext(String operation, Car car, String... changeDetails) {
        String title;
        String message;
        String actionType = "CAR_" + operation;
        
        switch (operation.toUpperCase()) {
            case "CREATE":
                title = messageService.getMessage("notification.car.create.title");
                message = messageService.getMessage("notification.car.create.body",
                    car.getName() != null ? car.getName() : "",
                    car.getModel() != null ? car.getModel() : "",
                    car.getPlateNumber() != null ? car.getPlateNumber() : ""
                );
                break;
                
            case "UPDATE":
                title = messageService.getMessage("notification.car.update.title");
                String changes = (changeDetails != null && changeDetails.length > 0) 
                    ? changeDetails[0] 
                    : messageService.getMessage("notification.car.change.default");
                message = messageService.getMessage("notification.car.update.body",
                    car.getName() != null ? car.getName() : "",
                    car.getModel() != null ? car.getModel() : "",
                    changes
                );
                break;
                
            case "DELETE":
                title = messageService.getMessage("notification.car.delete.title");
                message = messageService.getMessage("notification.car.delete.body",
                    car.getName() != null ? car.getName() : "",
                    car.getModel() != null ? car.getModel() : "",
                    car.getPlateNumber() != null ? car.getPlateNumber() : ""
                );
                break;
                
            default:
                title = "إشعار سيارة";
                message = "تم تنفيذ عملية على السيارة";
        }
        
        return NotificationContext.builder()
            .operation(operation)
            .title(title)
            .message(message)
            .actionType(actionType)
            .entity(car)
            .entityId(car.getId())
            .entityName(car.getName())
            .additionalData(Map.of(
                "carId", String.valueOf(car.getId()),
                "carPlate", car.getPlateNumber() != null ? car.getPlateNumber() : "",
                "carModel", car.getModel() != null ? car.getModel() : "",
                "operation", operation
            ))
            .build();
    }

    // ==================== PERSON NOTIFICATIONS ====================
    
    @Transactional
    public void notifyPersonOperation(NotificationContext context) {
        try {
            AppUser currentUser = helper.getCurrentUser();
            Long companyId = helper.getCurrentCompanyId();
            
            if (currentUser == null || companyId == null) {
                log.warn(messageService.getMessage("notification.person.error.no.user"));
                return;
            }
            
            List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
            
            if (companyUsers.isEmpty()) {
                log.info(messageService.getMessage("notification.person.error.no.token", companyId));
                return;
            }
            
            log.info(messageService.getMessage("notification.person.log.sending", 
                context.getOperation(), companyUsers.size(), companyId));
            
            NotificationRequest request = NotificationRequest.builder()
                .title(context.getTitle())
                .message(context.getMessage())
                .actionBy(currentUser.getEmail())
                .actionType(context.getActionType())
                .actionDate(LocalDateTime.now())
                .companyId(companyId)
                .targetUserIds(companyUsers.stream()
                    .map(AppUser::getId)
                    .collect(Collectors.toList()))
                .build();
            
            Map<String, String> additionalData = new HashMap<>();
            if (context.getAdditionalData() != null) {
                context.getAdditionalData().forEach((k, v) -> 
                    additionalData.put(k, String.valueOf(v)));
            }
            request.setAdditionalData(additionalData);
            
            notificationService.sendNotification(request);
            
            log.info(messageService.getMessage("notification.person.log.success", 
                context.getOperation(), context.getEntityId()));
            
        } catch (Exception e) {
            log.error(messageService.getMessage("notification.person.log.failed", 
                context.getOperation(), context.getEntityId(), e.getMessage()));
        }
    }
    
    /**
     * إنشاء سياق الإشعار للشخص (مع رسائل من ملف properties)
     */
    public NotificationContext createPersonContext(String operation, Person person, String... changeDetails) {
        String title;
        String message;
        String actionType = "PERSON_" + operation;
        
        // Get person full name
        String fullName = getPersonFullName(person);
        
        switch (operation.toUpperCase()) {
            case "CREATE":
                title = messageService.getMessage("notification.person.create.title");
                message = messageService.getMessage("notification.person.create.body",
                    fullName,
                    person.getPhoneNumber() != null ? person.getPhoneNumber() : "",
                    person.getNationalId() != null ? person.getNationalId() : ""
                );
                break;
                
            case "UPDATE":
                title = messageService.getMessage("notification.person.update.title");
                String changes = (changeDetails != null && changeDetails.length > 0) 
                    ? changeDetails[0] 
                    : messageService.getMessage("notification.person.change.default");
                message = messageService.getMessage("notification.person.update.body",
                    fullName,
                    person.getPhoneNumber() != null ? person.getPhoneNumber() : "",
                    changes
                );
                break;
                
            case "DELETE":
                title = messageService.getMessage("notification.person.delete.title");
                message = messageService.getMessage("notification.person.delete.body",
                    fullName,
                    person.getPhoneNumber() != null ? person.getPhoneNumber() : "",
                    person.getNationalId() != null ? person.getNationalId() : ""
                );
                break;
                
            default:
                title = "إشعار شخص";
                message = "تم تنفيذ عملية على بيانات الشخص";
        }
        
        return NotificationContext.builder()
            .operation(operation)
            .title(title)
            .message(message)
            .actionType(actionType)
            .entity(person)
            .entityId(person.getId())
            .entityName(fullName)
            .additionalData(Map.of(
                "personId", String.valueOf(person.getId()),
                "personName", fullName,
                "personNationalId", person.getNationalId() != null ? person.getNationalId() : "",
                "personPhone", person.getPhoneNumber() != null ? person.getPhoneNumber() : "",
                "operation", operation
            ))
            .build();
    }
    
    /**
     * Get person full name
     */
    private String getPersonFullName(Person person) {
        StringBuilder fullName = new StringBuilder();
        
        if (person.getFirstName() != null && !person.getFirstName().isEmpty()) {
            fullName.append(person.getFirstName());
        }
        if (person.getFatherName() != null && !person.getFatherName().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getFatherName());
        }
        if (person.getGrandfatherName() != null && !person.getGrandfatherName().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getGrandfatherName());
        }
        if (person.getFourthName() != null && !person.getFourthName().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getFourthName());
        }
        if (person.getSurname() != null && !person.getSurname().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getSurname());
        }
        
        return fullName.length() > 0 ? fullName.toString() : "بدون اسم";
    }
    
    /**
     * Generate change details for person update
     */
    public String generatePersonChangeDetails(Person oldPerson, Person newPerson) {
        StringBuilder changes = new StringBuilder();
        
        // Check name changes
        String oldName = getPersonFullName(oldPerson);
        String newName = getPersonFullName(newPerson);
        if (!oldName.equals(newName)) {
            changes.append(messageService.getMessage("notification.person.change.name", oldName, newName));
            changes.append("\n");
        }
        
        // Check national ID change
        if (oldPerson.getNationalId() != null && !oldPerson.getNationalId().equals(newPerson.getNationalId())) {
            changes.append(messageService.getMessage("notification.person.change.nationalId", 
                oldPerson.getNationalId(), newPerson.getNationalId()));
            changes.append("\n");
        }
        
        // Check email change (if exists in Person entity - you might need to add email field)
        // Note: Your Person model doesn't have email field. Add if needed.
        
        // Check phone change
        if (oldPerson.getPhoneNumber() != null && !oldPerson.getPhoneNumber().equals(newPerson.getPhoneNumber())) {
            changes.append(messageService.getMessage("notification.person.change.phone", 
                oldPerson.getPhoneNumber(), newPerson.getPhoneNumber()));
            changes.append("\n");
        }
        
        // Check residence card number change
        if (oldPerson.getResidenceCardNo() != null && !oldPerson.getResidenceCardNo().equals(newPerson.getResidenceCardNo())) {
            changes.append(messageService.getMessage("notification.person.change.residenceCard", 
                oldPerson.getResidenceCardNo(), newPerson.getResidenceCardNo()));
            changes.append("\n");
        }
        
        // Check residence change
        if (oldPerson.getResidence() != null && !oldPerson.getResidence().equals(newPerson.getResidence())) {
            changes.append(messageService.getMessage("notification.person.change.residence", 
                oldPerson.getResidence(), newPerson.getResidence()));
            changes.append("\n");
        }
        
        return changes.length() > 0 ? changes.toString() : messageService.getMessage("notification.person.change.default");
    }

    // ==================== PAYMENT NOTIFICATIONS ====================

@Transactional
public void notifyPaymentOperation(NotificationContext context) {
    try {
        AppUser currentUser = helper.getCurrentUser();
        Long companyId = helper.getCurrentCompanyId();
        
        if (currentUser == null || companyId == null) {
            log.warn(messageService.getMessage("notification.payment.error.no.user"));
            return;
        }
        
        List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
        
        if (companyUsers.isEmpty()) {
            log.info(messageService.getMessage("notification.payment.error.no.token", companyId));
            return;
        }
        
        log.info(messageService.getMessage("notification.payment.log.sending", 
            context.getOperation(), companyUsers.size(), companyId));
        
        NotificationRequest request = NotificationRequest.builder()
            .title(context.getTitle())
            .message(context.getMessage())
            .actionBy(currentUser.getEmail())
            .actionType(context.getActionType())
            .actionDate(LocalDateTime.now())
            .companyId(companyId)
            .targetUserIds(companyUsers.stream()
                .map(AppUser::getId)
                .collect(Collectors.toList()))
            .build();
        
        Map<String, String> additionalData = new HashMap<>();
        if (context.getAdditionalData() != null) {
            context.getAdditionalData().forEach((k, v) -> 
                additionalData.put(k, String.valueOf(v)));
        }
        request.setAdditionalData(additionalData);
        
        notificationService.sendNotification(request);
        
        log.info(messageService.getMessage("notification.payment.log.success", 
            context.getOperation(), context.getEntityId()));
        
    } catch (Exception e) {
        log.error(messageService.getMessage("notification.payment.log.failed", 
            context.getOperation(), context.getEntityId(), e.getMessage()));
    }
}
}