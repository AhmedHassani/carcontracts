package com.ahd.backend.carcontracts.car.service;

import com.ahd.backend.carcontracts.r2.R2FileStorageService;
import com.ahd.backend.carcontracts.r2.R2UrlService;
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
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;  // ✅ أضف هذا الاستيراد
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.service.MessageService;  // ✅ أضف هذا الاستيراد
import com.ahd.backend.carcontracts.notification.service.NotificationSender;  // ✅ أضف هذا الاستيراد
import com.ahd.backend.carcontracts.notification.service.NotificationService;
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
    private final R2FileStorageService imageStorageService;
    private final R2UrlService r2UrlService;
    private final R2FileStorageService storageService;
    private final CarAttachmentRepository carAttachmentRepo;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final Helper helper;
    private final PersonRepository personRepository;
    private final NotificationService notificationService;
    private final NotificationSender notificationSender;  // ✅ أضف هذا
    private final MessageService messageService;  // ✅ أضف هذا

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
       
        if (files != null && !files.isEmpty()) {
            for (MultipartFile f : files) {
                String key = storageService.upload(f);
                CarAttachment att = CarAttachment.builder()
                        .car(car)
                        .fileKey(r2UrlService.getImageUrl(key))
                        .mimeType(Optional.ofNullable(f.getContentType())
                                .orElse("application/octet-stream"))
                        .build();
                car.getAttachments().add(att);
            }
        }
        
        car.setStatus("Pending");
        Car saved = carRepository.save(car);
        
        // ✅ إرسال إشعار الإضافة
        // NotificationContext context = notificationSender.createCarContext("CREATE", saved);
        // notificationSender.notifyCarOperation(context);
        
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
    // Check if the patch has ANY fields at all
    if (patch == null || !patch.hasAnyField()) {
        throw new BadRequestException("Update payload must contain at least one field");
    }
    
    Car car = carRepository.findWithAttachmentsByIdAndCompanyId(id, getCompanyId())
            .orElseThrow(() -> new ResourceNotFoundException("Car " + id + " not found"));
    
    // Store old values for notification
    String oldName = car.getName();
    String oldPlate = car.getPlateNumber();
    
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

    if (!car.getStatus().equals("Pending") && patch.getCurrentPossessorId() != null) {
        throw new BadRequestException("Cannot change current possessor of an active car");
    }
    
    // Handle currentPossessorId - allows setting to null
    // Check if the field was explicitly included in the request (even if null)
    if (patch.getCurrentPossessorId() != null) {
        // Set to a specific person
        Person possessor = personRepository.findById(patch.getCurrentPossessorId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + patch.getCurrentPossessorId()));
        car.setCurrentPossessor(possessor);
    } else if (patch.hasAnyField() && patch.getCurrentPossessorId() == null) {
        // The field was included in the request with null value
        // We know it was included because hasAnyField() is true
        car.setCurrentPossessor(null);
    }
    
    // Handle carPrice - sets to "0" if null
    // Check if the field was explicitly included in the request (even if null)
    if (patch.getCarPrice() != null) {
        car.setCarPrice(patch.getCarPrice());
    } else if (patch.hasAnyField() && patch.getCarPrice() == null) {
        // The field was included in the request with null value
        car.setCarPrice("0");
    }
    
    // Apply all other updates using the mapper
    CarMapper.updateEntity(car, patch);
    
    // Save the updated car
    Car saved = carRepository.save(car);
    
    // Generate change details for notification
    String changeDetails = generateChangeDetailsFromProperties(oldName, oldPlate, saved);
    NotificationContext context = notificationSender.createCarContext("UPDATE", saved, changeDetails);
    notificationSender.notifyCarOperation(context);
    
    return CarMapper.toDto(saved);
}
    @Auditable(operation = "حذف سيارة", captureArgs = true, captureResult = true)
    public void softDeleteCar(Long id) {
        Car car = carRepository.findByIdAndCompanyIdAndDeletedFalse(id, getCompanyId())
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));

        if (!Objects.equals(car.getStatus(), "Active")) {
            String carName = car.getName();
            String carPlate = car.getPlateNumber();
            String carModel = car.getModel();
            
            car.setDeleted(true);
            carRepository.save(car);
            
            // ✅ إرسال إشعار الحذف
            NotificationContext context = notificationSender.createCarContext("DELETE", car);
            notificationSender.notifyCarOperation(context);
        } else {
            throw new IllegalStateException(messageService.getMessage("notification.car.delete.active.error"));
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
            String url = r2UrlService.getImageUrl(key);
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
        
        return result;
    }

    public Long getCompanyId() {
        return helper.getCurrentCompanyId();
    }
    
   
    private String generateChangeDetailsFromProperties(String oldName, String oldPlate, Car updatedCar) {
        StringBuilder changes = new StringBuilder();
        
        if (oldName != null && !oldName.equals(updatedCar.getName())) {
            changes.append(messageService.getMessage("notification.car.change.name", oldName, updatedCar.getName()));
            changes.append("\n");
        }
        
        if (oldPlate != null && !oldPlate.equals(updatedCar.getPlateNumber())) {
            changes.append(messageService.getMessage("notification.car.change.plate", oldPlate, updatedCar.getPlateNumber()));
        }
        
        return changes.length() > 0 ? changes.toString() : messageService.getMessage("notification.car.change.default");
    }
}