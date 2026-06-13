package com.ahd.backend.carcontracts.notification.service;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.util.Helper;
import com.ahd.backend.carcontracts.company.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ahd.backend.carcontracts.dropDownList.model.OptionDropDown;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ahd.backend.carcontracts.contract.model.Contracts;
import java.time.LocalDate;
import com.ahd.backend.carcontracts.authorization.model.Authorization;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import java.util.Set;
import java.util.HashSet;

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

    // ==================== CONTRACT NOTIFICATIONS ====================
    
    private String formatDate(LocalDate date) {
        if (date == null) return "غير محدد";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
    
    private String getPersonName(Person person) {
        if (person == null) return "غير محدد";
        return getPersonFullName(person);
    }
    
    public NotificationContext createContractContext(String operation, Contracts contract) {
        return createContractContext(operation, contract, null);
    }
    
    public NotificationContext createContractContext(String operation, Contracts contract, String changeDetails) {
        String titleKey = "notification.contract." + operation.toLowerCase() + ".title";
        String bodyKey = "notification.contract." + operation.toLowerCase() + ".body";
        
        String buyerName = contract.getBuyer() != null ? 
            getPersonFullName(contract.getBuyer()) : "غير محدد";
        
        String title = messageService.getMessage(titleKey);
        String message;
        
        if ("UPDATE".equals(operation) && changeDetails != null && !changeDetails.isEmpty()) {
            message = messageService.getMessage(bodyKey, contract.getContractNumber(), changeDetails);
        } else if ("CREATE".equals(operation)) {
            message = messageService.getMessage(bodyKey, buyerName, contract.getContractNumber());
        } else if ("DELETE".equals(operation)) {
            message = messageService.getMessage(bodyKey, buyerName, contract.getContractNumber());
        } else {
            message = messageService.getMessage(bodyKey, contract.getContractNumber(), changeDetails != null ? changeDetails : "");
        }
        
        return NotificationContext.builder()
                .operation(operation)
                .title(title)
                .message(message)
                .actionType("CONTRACT_" + operation)
                .entity(contract)
                .entityId(contract.getId())
                .entityName("Contract")
                .additionalData(Map.of(
                    "contractNumber", contract.getContractNumber(),
                    "changeDetails", changeDetails != null ? changeDetails : "",
                    "buyerName", buyerName
                ))
                .build();
    }
    
    public String generateContractChangeDetails(Contracts oldContract, Contracts newContract) {
        List<String> changes = new ArrayList<>();
        
        // Check contract date change
        if (!Objects.equals(oldContract.getContractDate(), newContract.getContractDate())) {
            changes.add(messageService.getMessage("notification.contract.change.contractDate",
                formatDate(oldContract.getContractDate()),
                formatDate(newContract.getContractDate())));
        }
        
        // Check onus change
        if (oldContract.isOnus() != newContract.isOnus()) {
            changes.add(messageService.getMessage("notification.contract.change.onus",
                oldContract.isOnus() ? "نعم" : "لا",
                newContract.isOnus() ? "نعم" : "لا"));
        }
        
        // Check seller change
        Long oldSellerId = oldContract.getSeller() != null ? oldContract.getSeller().getId() : null;
        Long newSellerId = newContract.getSeller() != null ? newContract.getSeller().getId() : null;
        if (!Objects.equals(oldSellerId, newSellerId)) {
            changes.add(messageService.getMessage("notification.contract.change.seller",
                getPersonName(oldContract.getSeller()),
                getPersonName(newContract.getSeller())));
        }
        
        // Check buyer change
        Long oldBuyerId = oldContract.getBuyer() != null ? oldContract.getBuyer().getId() : null;
        Long newBuyerId = newContract.getBuyer() != null ? newContract.getBuyer().getId() : null;
        if (!Objects.equals(oldBuyerId, newBuyerId)) {
            changes.add(messageService.getMessage("notification.contract.change.buyer",
                getPersonName(oldContract.getBuyer()),
                getPersonName(newContract.getBuyer())));
        }
        
        // Check guarantor change
        Long oldGuarantorId = oldContract.getGuarantor() != null ? oldContract.getGuarantor().getId() : null;
        Long newGuarantorId = newContract.getGuarantor() != null ? newContract.getGuarantor().getId() : null;
        if (!Objects.equals(oldGuarantorId, newGuarantorId)) {
            changes.add(messageService.getMessage("notification.contract.change.guarantor",
                getPersonName(oldContract.getGuarantor()),
                getPersonName(newContract.getGuarantor())));
        }
        
        // Check possessor change
        Long oldPossessorId = oldContract.getPossessor() != null ? oldContract.getPossessor().getId() : null;
        Long newPossessorId = newContract.getPossessor() != null ? newContract.getPossessor().getId() : null;
        if (!Objects.equals(oldPossessorId, newPossessorId)) {
            changes.add(messageService.getMessage("notification.contract.change.possessor",
                getPersonName(oldContract.getPossessor()),
                getPersonName(newContract.getPossessor())));
        }
        
        if (changes.isEmpty()) {
            changes.add(messageService.getMessage("notification.contract.change.default"));
        }
        
        return String.join("\n", changes);
    }
    
    public void notifyContractOperation(NotificationContext context) {
        try {
            AppUser currentUser = helper.getCurrentUser();
            if (currentUser == null) {
                log.warn(messageService.getMessage("notification.contract.error.no.user"));
                return;
            }
            
            Long companyId = helper.getCurrentCompanyId();
            if (companyId == null) {
                log.warn("No company context found for contract notification");
                return;
            }
            
            // Get users with FCM tokens in this company
            List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
            
            if (companyUsers.isEmpty()) {
                log.info(messageService.getMessage("notification.contract.error.no.token", companyId));
                return;
            }
            
            log.info(messageService.getMessage("notification.contract.log.sending",
                context.getOperation(), companyUsers.size(), companyId));
            
            Map<String, String> additionalData = new HashMap<>();
            if (context.getAdditionalData() != null) {
                context.getAdditionalData().forEach((k, v) -> 
                    additionalData.put(k, String.valueOf(v)));
            }
            
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
                    .additionalData(additionalData)
                    .build();
            
            notificationService.sendNotification(request);
            
            log.info(messageService.getMessage("notification.contract.log.success",
                context.getOperation(),
                String.valueOf(context.getEntityId())));
                
        } catch (Exception e) {
            log.error(messageService.getMessage("notification.contract.log.failed",
                context.getOperation(),
                String.valueOf(context.getEntityId()),
                e.getMessage()), e);
        }
    }

    // ==================== AUTHORIZATION NOTIFICATIONS ====================

    private String getAuthorizationBuyerName(Authorization authorization) {
        if (authorization.getBuyer() == null) return "غير محدد";
        return getPersonFullName(authorization.getBuyer());
    }
    
    private String getAuthorizationCarInfo(Authorization authorization) {
        if (authorization.getCar() == null) return "غير محدد";
        Car car = authorization.getCar();
        String carInfo = "";
        if (car.getName() != null) carInfo += car.getName();
        if (car.getPlateNumber() != null) {
            if (!carInfo.isEmpty()) carInfo += " - ";
            carInfo += car.getPlateNumber();
        }
        return carInfo.isEmpty() ? "غير محدد" : carInfo;
    }
    
    public NotificationContext createAuthorizationContext(String operation, Authorization authorization) {
        return createAuthorizationContext(operation, authorization, null);
    }
    
    public NotificationContext createAuthorizationContext(String operation, Authorization authorization, String changeDetails) {
        String titleKey = "notification.authorization." + operation.toLowerCase() + ".title";
        String bodyKey = "notification.authorization." + operation.toLowerCase() + ".body";
        
        String buyerName = getAuthorizationBuyerName(authorization);
        String carInfo = getAuthorizationCarInfo(authorization);
        
        String title = messageService.getMessage(titleKey);
        String message;
        
        if ("BUYER_CHANGE".equals(operation)) {
            message = messageService.getMessage("notification.authorization.buyer.change.body",
                authorization.getAuthorizationNumber(),
                changeDetails != null ? changeDetails : "");
        } else if ("UPDATE".equals(operation) && changeDetails != null && !changeDetails.isEmpty()) {
            message = messageService.getMessage(bodyKey, authorization.getAuthorizationNumber(), changeDetails);
        } else if ("CREATE".equals(operation)) {
            message = messageService.getMessage(bodyKey, buyerName, carInfo, authorization.getAuthorizationNumber());
        } else if ("DELETE".equals(operation)) {
            message = messageService.getMessage(bodyKey, buyerName, carInfo, authorization.getAuthorizationNumber());
        } else {
            message = messageService.getMessage(bodyKey, authorization.getAuthorizationNumber(), changeDetails != null ? changeDetails : "");
        }
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("authorizationNumber", authorization.getAuthorizationNumber());
        additionalData.put("buyerName", buyerName);
        additionalData.put("carInfo", carInfo);
        additionalData.put("operation", operation);
        if (changeDetails != null) {
            additionalData.put("changeDetails", changeDetails);
        }
        
        return NotificationContext.builder()
                .operation(operation)
                .title(title)
                .message(message)
                .actionType("AUTHORIZATION_" + operation)
                .entity(authorization)
                .entityId(authorization.getId())
                .entityName("Authorization")
                .additionalData(additionalData)
                .build();
    }
    
    public String generateAuthorizationChangeDetails(Authorization oldAuth, Authorization newAuth) {
        List<String> changes = new ArrayList<>();
        
        // Check authorization number change
        if (!Objects.equals(oldAuth.getAuthorizationNumber(), newAuth.getAuthorizationNumber())) {
            changes.add(messageService.getMessage("notification.authorization.change.number",
                oldAuth.getAuthorizationNumber(), newAuth.getAuthorizationNumber()));
        }
        
        // Check authorization date change
        if (!Objects.equals(oldAuth.getAuthorizationDate(), newAuth.getAuthorizationDate())) {
            changes.add(messageService.getMessage("notification.authorization.change.date",
                formatDate(oldAuth.getAuthorizationDate()),
                formatDate(newAuth.getAuthorizationDate())));
        }
        
        // Check company agent change
        if (!Objects.equals(oldAuth.getCompanyAgent(), newAuth.getCompanyAgent())) {
            changes.add(messageService.getMessage("notification.authorization.change.agent",
                oldAuth.getCompanyAgent() != null ? oldAuth.getCompanyAgent() : "غير محدد",
                newAuth.getCompanyAgent() != null ? newAuth.getCompanyAgent() : "غير محدد"));
        }
        
        // Check buyer change
        Long oldBuyerId = oldAuth.getBuyer() != null ? oldAuth.getBuyer().getId() : null;
        Long newBuyerId = newAuth.getBuyer() != null ? newAuth.getBuyer().getId() : null;
        if (!Objects.equals(oldBuyerId, newBuyerId)) {
            changes.add(messageService.getMessage("notification.authorization.change.buyer",
                getPersonName(oldAuth.getBuyer()),
                getPersonName(newAuth.getBuyer())));
        }
        
        // Check car change
        Long oldCarId = oldAuth.getCar() != null ? oldAuth.getCar().getId() : null;
        Long newCarId = newAuth.getCar() != null ? newAuth.getCar().getId() : null;
        if (!Objects.equals(oldCarId, newCarId)) {
            String oldCarInfo = oldAuth.getCar() != null ? 
                (oldAuth.getCar().getName() + " - " + oldAuth.getCar().getPlateNumber()) : "غير محدد";
            String newCarInfo = newAuth.getCar() != null ? 
                (newAuth.getCar().getName() + " - " + newAuth.getCar().getPlateNumber()) : "غير محدد";
            changes.add(messageService.getMessage("notification.authorization.change.car", oldCarInfo, newCarInfo));
        }
        
        if (changes.isEmpty()) {
            changes.add(messageService.getMessage("notification.authorization.change.default"));
        }
        
        return String.join("\n", changes);
    }
    
    public void notifyAuthorizationOperation(NotificationContext context) {
        try {
            AppUser currentUser = helper.getCurrentUser();
            if (currentUser == null) {
                log.warn(messageService.getMessage("notification.authorization.error.no.user"));
                return;
            }
            
            Long companyId = helper.getCurrentCompanyId();
            if (companyId == null) {
                log.warn("No company context found for authorization notification");
                return;
            }
            
            // Get users with FCM tokens in this company
            List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
            
            if (companyUsers.isEmpty()) {
                log.info(messageService.getMessage("notification.authorization.error.no.token", companyId));
                return;
            }
            
            log.info(messageService.getMessage("notification.authorization.log.sending",
                context.getOperation(), companyUsers.size(), companyId));
            
            // Convert Map<String, Object> to Map<String, String>
            Map<String, String> additionalData = new HashMap<>();
            if (context.getAdditionalData() != null) {
                context.getAdditionalData().forEach((k, v) -> 
                    additionalData.put(k, String.valueOf(v)));
            }
            
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
                    .additionalData(additionalData)
                    .build();
            
            notificationService.sendNotification(request);
            
            log.info(messageService.getMessage("notification.authorization.log.success",
                context.getOperation(),
                String.valueOf(context.getEntityId())));
                
        } catch (Exception e) {
            log.error(messageService.getMessage("notification.authorization.log.failed",
                context.getOperation(),
                String.valueOf(context.getEntityId()),
                e.getMessage()), e);
        }
    }

    // ==================== COMPANY NOTIFICATIONS ====================

    /**
     * Create notification context for company operations
     */
    public NotificationContext createCompanyContext(String operation, Company company, String... changeDetails) {
        String title;
        String message;
        String actionType = "COMPANY_" + operation;
        
        switch (operation.toUpperCase()) {
            case "CREATE":
                title = messageService.getMessage("notification.company.create.title");
                message = messageService.getMessage("notification.company.create.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    company.getOwnerName() != null ? company.getOwnerName() : "",
                    company.getCode() != null ? company.getCode() : ""
                );
                break;
                
            case "UPDATE":
                title = messageService.getMessage("notification.company.update.title");
                String changes = (changeDetails != null && changeDetails.length > 0) 
                    ? changeDetails[0] 
                    : messageService.getMessage("notification.company.change.default");
                message = messageService.getMessage("notification.company.update.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    changes
                );
                break;
                
            case "DELETE":
                title = messageService.getMessage("notification.company.delete.title");
                message = messageService.getMessage("notification.company.delete.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    company.getCode() != null ? company.getCode() : ""
                );
                break;
                
            case "STATUS_CHANGE":
                title = messageService.getMessage("notification.company.status.title");
                String statusChange = (changeDetails != null && changeDetails.length > 0) 
                    ? changeDetails[0] 
                    : messageService.getMessage("notification.company.change.default");
                message = messageService.getMessage("notification.company.status.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    statusChange
                );
                break;
                
            case "USER_ADD":
                title = messageService.getMessage("notification.company.user.add.title");
                message = messageService.getMessage("notification.company.user.add.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    changeDetails != null && changeDetails.length > 0 ? changeDetails[0] : ""
                );
                break;
                
            case "USER_REMOVE":
                title = messageService.getMessage("notification.company.user.remove.title");
                message = messageService.getMessage("notification.company.user.remove.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    changeDetails != null && changeDetails.length > 0 ? changeDetails[0] : ""
                );
                break;
                
            case "USER_ROLE_UPDATE":
                title = messageService.getMessage("notification.company.user.role.title");
                message = messageService.getMessage("notification.company.user.role.body",
                    company.getCompanyName() != null ? company.getCompanyName() : "",
                    changeDetails != null && changeDetails.length > 0 ? changeDetails[0] : ""
                );
                break;
                
            default:
                title = messageService.getMessage("notification.company.default.title");
                message = messageService.getMessage("notification.company.default.body", company.getCompanyName());
        }
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("companyId", String.valueOf(company.getId()));
        additionalData.put("companyName", company.getCompanyName() != null ? company.getCompanyName() : "");
        additionalData.put("companyCode", company.getCode() != null ? company.getCode() : "");
        additionalData.put("operation", operation);
        if (changeDetails != null && changeDetails.length > 0) {
            additionalData.put("changeDetails", changeDetails[0]);
        }
        
        return NotificationContext.builder()
            .operation(operation)
            .title(title)
            .message(message)
            .actionType(actionType)
            .entity(company)
            .entityId(company.getId())
            .entityName(company.getCompanyName())
            .additionalData(additionalData)
            .build();
    }
    
    /**
     * Generate change details for company update
     */
    public String generateCompanyChangeDetails(Company oldCompany, Company newCompany) {
        List<String> changes = new ArrayList<>();
        
        // Check company name change
        if (!Objects.equals(oldCompany.getCompanyName(), newCompany.getCompanyName())) {
            changes.add(messageService.getMessage("notification.company.change.name",
                oldCompany.getCompanyName(), newCompany.getCompanyName()));
        }
        
        // Check owner name change
        if (!Objects.equals(oldCompany.getOwnerName(), newCompany.getOwnerName())) {
            changes.add(messageService.getMessage("notification.company.change.owner",
                oldCompany.getOwnerName(), newCompany.getOwnerName()));
        }
        
        // Check owner contact change
        if (!Objects.equals(oldCompany.getOwnerContact(), newCompany.getOwnerContact())) {
            changes.add(messageService.getMessage("notification.company.change.contact",
                oldCompany.getOwnerContact(), newCompany.getOwnerContact()));
        }
        
        // Check user count change
        if (!Objects.equals(oldCompany.getUserCount(), newCompany.getUserCount())) {
            changes.add(messageService.getMessage("notification.company.change.userCount",
                String.valueOf(oldCompany.getUserCount()), String.valueOf(newCompany.getUserCount())));
        }
        
        // Check location change
        if (!Objects.equals(oldCompany.getCompanyLocation(), newCompany.getCompanyLocation())) {
            changes.add(messageService.getMessage("notification.company.change.location",
                oldCompany.getCompanyLocation() != null ? oldCompany.getCompanyLocation() : "غير محدد",
                newCompany.getCompanyLocation() != null ? newCompany.getCompanyLocation() : "غير محدد"));
        }
        
        // Check expiration date change
        if (!Objects.equals(oldCompany.getExpirationDate(), newCompany.getExpirationDate())) {
            changes.add(messageService.getMessage("notification.company.change.expirationDate",
                formatDate(oldCompany.getExpirationDate()),
                formatDate(newCompany.getExpirationDate())));
        }
        
        // Check status change
        if (oldCompany.getStatus() != newCompany.getStatus()) {
            changes.add(messageService.getMessage("notification.company.change.status",
                oldCompany.getStatus() != null ? oldCompany.getStatus().toString() : "غير محدد",
                newCompany.getStatus() != null ? newCompany.getStatus().toString() : "غير محدد"));
        }
        
        if (changes.isEmpty()) {
            changes.add(messageService.getMessage("notification.company.change.default"));
        }
        
        return String.join("\n", changes);
    }
    
    /**
     * Generate user change details
     */
    public String generateUserChangeDetails(String userName, String oldRole, String newRole) {
        return messageService.getMessage("notification.company.user.role.details",
            userName, oldRole, newRole);
    }
    
    /**
     * Send company notification
     */
  @Transactional
public void notifyCompanyOperation(NotificationContext context) {
    try {
        AppUser currentUser = helper.getCurrentUser();
        Long companyId = helper.getCurrentCompanyId();
        
        if (currentUser == null) {
            log.warn(messageService.getMessage("notification.company.error.no.user"));
            return;
        }
        
        // ✅ Get users with FCM tokens in this company
        List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
        
        // ✅ ALSO get Super Admin and other admin users (users with no company or ROLE_SUPER_ADMIN)
        List<AppUser> adminUsers = userRepository.findAll().stream()
            .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
            .filter(user -> {
                // Check if user is Super Admin or has no company
                boolean isSuperAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_SUPER_ADMIN"));
                boolean hasNoCompany = true;
                // Check if user has any company association
                // You may need to check via CompanyUser repository
                return isSuperAdmin || hasNoCompany;
            })
            .collect(Collectors.toList());
        
        // ✅ Combine both lists and remove duplicates
        Set<AppUser> allTargetUsers = new HashSet<>();
        allTargetUsers.addAll(companyUsers);
        allTargetUsers.addAll(adminUsers);
        
        if (allTargetUsers.isEmpty()) {
            log.info(messageService.getMessage("notification.company.error.no.token"));
            return;
        }
        
        log.info(messageService.getMessage("notification.company.log.sending",
            context.getOperation(), allTargetUsers.size()));
        
        Map<String, String> additionalData = new HashMap<>();
        if (context.getAdditionalData() != null) {
            context.getAdditionalData().forEach((k, v) -> 
                additionalData.put(k, String.valueOf(v)));
        }
        
        NotificationRequest request = NotificationRequest.builder()
            .title(context.getTitle())
            .message(context.getMessage())
            .actionBy(currentUser.getEmail())
            .actionType(context.getActionType())
            .actionDate(LocalDateTime.now())
            .companyId(companyId)
            .targetUserIds(allTargetUsers.stream()
                .map(AppUser::getId)
                .collect(Collectors.toList()))
            .additionalData(additionalData)
            .build();
        
        notificationService.sendNotification(request);
        
        log.info(messageService.getMessage("notification.company.log.success",
            context.getOperation(), context.getEntityId()));
        
    } catch (Exception e) {
        log.error(messageService.getMessage("notification.company.log.failed",
            context.getOperation(), context.getEntityId(), e.getMessage()));
    }
}
    // ==================== DROPDOWN NOTIFICATIONS ====================

/**
 * Create notification context for dropdown option operations
 */
public NotificationContext createDropDownContext(String operation, OptionDropDown option, String dropDownName, String... changeDetails) {
    String title;
    String message;
    String actionType = "DROPDOWN_" + operation;
    
    switch (operation.toUpperCase()) {
        case "CREATE":
            title = messageService.getMessage("notification.dropdown.create.title");
            message = messageService.getMessage("notification.dropdown.create.body",
                dropDownName != null ? dropDownName : "",
                option.getLabel() != null ? option.getLabel() : "",
                option.getValue() != null ? option.getValue() : ""
            );
            break;
            
        case "UPDATE":
            title = messageService.getMessage("notification.dropdown.update.title");
            String changes = (changeDetails != null && changeDetails.length > 0) 
                ? changeDetails[0] 
                : messageService.getMessage("notification.dropdown.change.default");
            message = messageService.getMessage("notification.dropdown.update.body",
                dropDownName != null ? dropDownName : "",
                option.getLabel() != null ? option.getLabel() : "",
                changes
            );
            break;
            
        case "DELETE":
            title = messageService.getMessage("notification.dropdown.delete.title");
            message = messageService.getMessage("notification.dropdown.delete.body",
                dropDownName != null ? dropDownName : "",
                option.getLabel() != null ? option.getLabel() : "",
                option.getValue() != null ? option.getValue() : ""
            );
            break;
            
        default:
            title = messageService.getMessage("notification.dropdown.default.title");
            message = messageService.getMessage("notification.dropdown.default.body", dropDownName);
    }
    
    Map<String, Object> additionalData = new HashMap<>();
    additionalData.put("optionId", String.valueOf(option.getId()));
    additionalData.put("optionLabel", option.getLabel() != null ? option.getLabel() : "");
    additionalData.put("optionValue", option.getValue() != null ? option.getValue() : "");
    additionalData.put("dropDownId", String.valueOf(option.getDropDownId()));
    additionalData.put("dropDownName", dropDownName != null ? dropDownName : "");
    additionalData.put("operation", operation);
    if (changeDetails != null && changeDetails.length > 0) {
        additionalData.put("changeDetails", changeDetails[0]);
    }
    
    return NotificationContext.builder()
        .operation(operation)
        .title(title)
        .message(message)
        .actionType(actionType)
        .entity(option)
        .entityId(option.getId())
        .entityName(option.getLabel())
        .additionalData(additionalData)
        .build();
}

/**
 * Send dropdown notification
 */
@Transactional
public void notifyDropDownOperation(NotificationContext context) {
    try {
        AppUser currentUser = helper.getCurrentUser();
        Long companyId = helper.getCurrentCompanyId();
        
        if (currentUser == null || companyId == null) {
            log.warn(messageService.getMessage("notification.dropdown.error.no.user"));
            return;
        }
        
        // Get users with FCM tokens in this company
        List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
        
        if (companyUsers.isEmpty()) {
            log.info(messageService.getMessage("notification.dropdown.error.no.token", companyId));
            return;
        }
        
        log.info(messageService.getMessage("notification.dropdown.log.sending",
            context.getOperation(), companyUsers.size(), companyId));
        
        Map<String, String> additionalData = new HashMap<>();
        if (context.getAdditionalData() != null) {
            context.getAdditionalData().forEach((k, v) -> 
                additionalData.put(k, String.valueOf(v)));
        }
        
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
            .additionalData(additionalData)
            .build();
        
        notificationService.sendNotification(request);
        
        log.info(messageService.getMessage("notification.dropdown.log.success",
            context.getOperation(), context.getEntityId()));
        
    } catch (Exception e) {
        log.error(messageService.getMessage("notification.dropdown.log.failed",
            context.getOperation(), context.getEntityId(), e.getMessage()));
    }
}



}