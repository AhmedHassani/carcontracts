package com.ahd.backend.carcontracts.car.service;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.mapper.CarMapper;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.model.CarAttachment;
import com.ahd.backend.carcontracts.car.repository.CarAttachmentRepository;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.DuplicateResourceException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.ahd.backend.carcontracts.appuser.models.AppUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {

    private final CarRepository carRepository;
    private final S3FileStorageService imageStorageService;
    private final S3UrlService s3UrlService;
    private final S3FileStorageService storageService;
    private final CarAttachmentRepository carAttachmentRepo;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final Helper helper;
    private final PersonRepository personRepository;
    
    // Add notification service
    private final com.ahd.backend.carcontracts.notification.service.NotificationService notificationService;

  @Transactional
@Auditable(operation = "انشاء سيارة", captureArgs = true, captureResult = true)
public CarResponseDTO createCar(CarRequestDTO dto, List<MultipartFile> files) {
    dto.setCompnayId(getCompanyId());
    
    // Check for existing chassis number
    if (carRepository.existsByChassisNumberAndCompanyIdAndStatus(dto.getChassisNumber(), getCompanyId(), "Pending") ||
        carRepository.existsByChassisNumberAndCompanyIdAndStatus(dto.getChassisNumber(), getCompanyId(), "Active")) {
        throw new DuplicateResourceException("ChassisNumber", dto.getPlateNumber(), 
            "Car with this Chassis Number already exists");
    }
    
    // Check for existing plate number
    if (carRepository.existsByPlateNumberAndWalletNumberAndTypeOfCarPlateAndCompanyIdAndStatus(dto.getPlateNumber(),
            dto.getWalletNumber(), dto.getTypeOfCarPlate(), getCompanyId(), "Pending") ||
        carRepository.existsByPlateNumberAndWalletNumberAndTypeOfCarPlateAndCompanyIdAndStatus(dto.getPlateNumber(),
            dto.getWalletNumber(), dto.getTypeOfCarPlate(), getCompanyId(), "Active")) {
        throw new DuplicateResourceException("plateNumber", dto.getPlateNumber(), 
            "Car with this plate number already exists");
    }

    Car car = CarMapper.toEntity(dto);
    
    // Handle currentPossessorId - convert from String to Long and fetch Person
    if (dto.getCurrentPossessorId() != null 
        && !dto.getCurrentPossessorId().isEmpty() 
        && !"null".equalsIgnoreCase(dto.getCurrentPossessorId())) {
        try {
            Long possessorId = Long.valueOf(dto.getCurrentPossessorId());
            Person possessor = personRepository.findById(possessorId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + possessorId));
            car.setCurrentPossessor(possessor);
        } catch (NumberFormatException e) {
            log.warn("Invalid currentPossessorId: {}", dto.getCurrentPossessorId());
        }
    }
    // If null, empty, or "null", car.currentPossessor stays null
    
    // Upload attachments
    if (files != null && !files.isEmpty()) {
        for (MultipartFile f : files) {
            String key = storageService.upload(f);
            CarAttachment att = CarAttachment.builder()
                    .car(car)
                    .fileKey(s3UrlService.getImageUrl(key))
                    .mimeType(Optional.ofNullable(f.getContentType())
                            .orElse("application/octet-stream"))
                    .build();
            car.getAttachments().add(att);
        }
    }
    
    car.setStatus("Pending");
    Car saved = carRepository.save(car);
    return CarMapper.toDto(saved);
}

    @Transactional(readOnly = true)
    public CarResponseDTO getCar(Long id) {
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + id + " not found"));
        return CarMapper.toDto(car);
    }

    @Transactional
    @Auditable(operation = "تحديث معلومات سيارة", captureArgs = true, captureResult = true)
    public CarResponseDTO updateCar(Long id, UpdateCarRequestDTO patch) {
        if (patch == null || patch.isEmpty()) {
            throw new BadRequestException("Update payload must contain at least one field");
        }
        
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(id, getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + id + " not found"));
        
        // Check chassis number uniqueness if changed
        if (patch.getChassisNumber() != null &&
                !patch.getChassisNumber().equals(car.getChassisNumber()) &&
                carRepository.existsByChassisNumberAndCompanyIdAndStatus(patch.getChassisNumber(), getCompanyId(), "Pending")) {
            throw new DuplicateResourceException("chassisNumber", patch.getChassisNumber(),
                    "Car with this chassis number already exists");
        }
        
        // Check plate number uniqueness if changed
        if (patch.getPlateNumber() != null &&
                !patch.getPlateNumber().equals(car.getPlateNumber()) &&
                carRepository.existsByPlateNumberAndWalletNumberAndTypeOfCarPlateAndCompanyIdAndStatus(patch.getPlateNumber(),
                        patch.getWalletNumber(), patch.getTypeOfCarPlate(), getCompanyId(), "Pending")) {
            throw new DuplicateResourceException("plateNumber", patch.getPlateNumber(),
                    "Car with this plate number already exists");
        }
        
        Car carUpdated = CarMapper.updateEntity(car, patch);
        Car saved = carRepository.save(carUpdated);
        return CarMapper.toDto(saved);
    }

    @Auditable(operation = "حذف سيارة", captureArgs = true, captureResult = true)
    public void softDeleteCar(Long id) {
        Car car = carRepository.findByIdAndCompanyIdAndDeletedFalse(id, getCompanyId())
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));

        if (!Objects.equals(car.getStatus(), "Active")) {
            car.setDeleted(true);
            carRepository.save(car);
        } else {
            throw new IllegalStateException("You can't remove a car that is in an active contract");
        }
    }

    @Transactional
    @Auditable(operation = "اضافة صور لسيارة", captureArgs = true, captureResult = true)
    public CarResponseDTO addAttachments(Long carId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("No files provided");
        }
        
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(carId, getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + carId + " not found"));
        
        for (MultipartFile file : files) {
            String key = storageService.upload(file);
            String url = s3UrlService.getImageUrl(key);
            CarAttachment att = CarAttachment.builder()
                    .car(car)
                    .fileKey(url)
                    .mimeType(Optional.ofNullable(file.getContentType())
                            .orElse("application/octet-stream"))
                    .build();
            car.getAttachments().add(att);
        }
        
        return CarMapper.toDto(car);
    }

    @Transactional
    @Auditable(operation = "حذف صور سيارة", captureArgs = true, captureResult = true)
    public CarResponseDTO deleteAttachment(Long carId, Long attachmentId) {
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(carId, getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + carId + " not found"));
        
        CarAttachment att = carAttachmentRepo.findByIdAndCarId(attachmentId, carId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment " + attachmentId + " not found for car " + carId));
        
        storageService.delete(att.getFileKey());
        car.getAttachments().remove(att);
        
        return CarMapper.toDto(car);
    }
    
    @Transactional
    public Page<CarResponseDTO> getAllCars(CarSearchCriteria criteria, Pageable pageable) {
        Sort.Direction dir = "DESC".equalsIgnoreCase(criteria.sortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = Optional.ofNullable(criteria.sortBy()).orElse("id");
        
        Pageable page = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(dir, sortBy));
        
        CarSearchCriteria enrichedCriteria = CarSearchCriteria.builder()
                .keyword(criteria.keyword())
                .sortBy(criteria.sortBy())
                .sortDirection(criteria.sortDirection())
                .type(criteria.type())
                .color(criteria.color())
                .engineType(criteria.engineType())
                .origin(criteria.origin())
                .deleted(criteria.deleted())
                .minKm(criteria.minKm())
                .maxKm(criteria.maxKm())
                .minCylinders(criteria.minCylinders())
                .maxCylinders(criteria.maxCylinders())
                .companyId(getCompanyId())
                .plateNumber(criteria.plateNumber())
                .chassisNumber(criteria.chassisNumber())
                .model(criteria.model())
                .status(criteria.status())
                .description(criteria.description())
                .name(criteria.name())
                .carPrice(criteria.carPrice())
                .currentPossessor(criteria.currentPossessor()) 
                .build();

        Specification<Car> spec = new CarSpecification(enrichedCriteria);
        Page<CarResponseDTO> result = carRepository.findAll(spec, page)
                .map(CarMapper::toDto);
        
        // SIMPLE TEST NOTIFICATION WITH STATIC DATA
    //    sendTestNotification();
        
        return result;
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
    
    /**
     * Simple test notification with static data
     * This is just for testing the notification system
     */
/**
 * Simple test notification with static data
 * This is just for testing the notification system
 */
/**
 * Simple test notification with static data
 * This is just for testing the notification system
 */
// private void sendTestNotification() {
//     try {
//         // Get the currently logged-in user (the one using Chrome)
//         AppUser currentUser = helper.getCurrentUser();
        
//         if (currentUser == null) {
//             log.warn("⚠️ No user logged in, cannot send test notification");
//             return;
//         }
        
//         log.info("📱 Sending test notification to current user: {} (ID: {})", 
//             currentUser.getEmail(), currentUser.getId());
//         log.info("📱 User has FCM token: {}", currentUser.getFcmToken() != null ? "Yes" : "No");
        
//         com.ahd.backend.carcontracts.notification.dto.NotificationRequest testNotification = 
//             com.ahd.backend.carcontracts.notification.dto.NotificationRequest.builder()
//                 .title("🚗 TEST NOTIFICATION")
//                 .message("You are viewing the cars list!")
//                 .actionBy(String.valueOf(currentUser.getId()))
//                 .actionType("VIEW_ALL_CARS")
//                 .actionDate(LocalDateTime.now())
//                 .companyId(getCompanyId())
//                 .targetUserIds(List.of(currentUser.getId()))  // Send to current user
//                 .build();
        
//         notificationService.sendNotification(testNotification);
//         log.info("✅ Test notification sent successfully to user: {}", currentUser.getEmail());
        
//     } catch (Exception e) {
//         log.error("❌ Failed to send test notification: {}", e.getMessage());
//     }
// }
}