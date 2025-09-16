package com.ahd.backend.carcontracts.car.service;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
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
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.model.CompanyUser;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyUserRepository;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.DuplicateResourceException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ahd.backend.carcontracts.S3.S3UrlService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final S3FileStorageService imageStorageService;
    private final S3UrlService s3UrlService;
    private final S3FileStorageService storageService;
    private final CarAttachmentRepository carAttachmentRepo;
    private final CompanyUserRepository companyUserRepository ;
    private final UserRepository userRepository ;
    private final Helper helper;
    @Transactional
    @Auditable(operation = "CREATE_CAR", captureArgs = true, captureResult = true)
    public CarResponseDTO createCar(CarRequestDTO dto, List<MultipartFile> files) {
        dto.setCompnayId(getCompanyId());
        if(carRepository.existsByChassisNumber(dto.getChassisNumber())
        && carRepository.existsByCompanyId(getCompanyId())
        && carRepository.existsByPlateNumber(dto.getPlateNumber())){
                throw new DuplicateResourceException(
                        "plateNumber", dto.getPlateNumber(), "Car with this plate number already exists");
        }

        Car car = CarMapper.toEntity(dto);
        if (files != null && !files.isEmpty()) {
            for (MultipartFile f : files) {
                String key = storageService.upload(f);
                CarAttachment att = CarAttachment.builder()
                        .car(car)
                        .fileKey(s3UrlService.getImageUrl(key))
                        .mimeType(
                                Optional.ofNullable(f.getContentType())
                                        .orElse("application/octet-stream"))
                        .build();
                car.getAttachments().add(att);
            }
        }
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
    @Auditable(operation = "UPDATE_CAR", captureArgs = true, captureResult = true)
    public CarResponseDTO updateCar(Long id, UpdateCarRequestDTO patch) {
        if (patch == null || patch.isEmpty()) {
            throw new BadRequestException("Update payload must contain at least one field");
        }
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + id + " not found"));
        if (patch.getChassisNumber() != null &&
                !patch.getChassisNumber().equals(car.getChassisNumber()) &&
                carRepository.existsByChassisNumber(patch.getChassisNumber())) {
            throw new DuplicateResourceException(
                    "chassisNumber", patch.getChassisNumber(),
                    "Car with this chassis number already exists");
        }
        if (patch.getPlateNumber() != null &&
                !patch.getPlateNumber().equals(car.getPlateNumber()) &&
                carRepository.existsByPlateNumber(patch.getPlateNumber())) {
            throw new DuplicateResourceException(
                    "plateNumber", patch.getPlateNumber(),
                    "Car with this plate number already exists");
        }
        Car carUpdated = CarMapper.updateEntity(car, patch);
        Car saved = carRepository.save(carUpdated);
        return CarMapper.toDto(saved);
    }
    @Auditable(operation = "DELETE_CAR", captureArgs = true, captureResult = true)
    public void softDeleteCar(Long id) {
        Car car = carRepository.findByIdAndCompanyIdAndDeletedFalse(id , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));
        car.setDeleted(true);
        carRepository.save(car);
    }

    @Transactional
    @Auditable(operation = "ADD_ATTACHMENT_CAR", captureArgs = true, captureResult = true)
    public CarResponseDTO addAttachments(Long carId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("No files provided");
        }
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(carId , getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + carId + " not found"));
        for (MultipartFile file : files) {
            String key  = storageService.upload(file);
            String url  = s3UrlService.getImageUrl(key);
            CarAttachment att = CarAttachment.builder()
                    .car(car)
                    .fileKey(url)
                    .mimeType(
                            Optional.ofNullable(file.getContentType())
                                    .orElse("application/octet-stream"))
                    .build();
            car.getAttachments().add(att);
        }
        return CarMapper.toDto(car);
    }

    @Transactional
    @Auditable(operation = "DELETE_ATTACHMENT_CAR", captureArgs = true, captureResult = true)
    public CarResponseDTO deleteAttachment(Long carId, Long attachmentId) {
        Car car = carRepository.findWithAttachmentsByIdAndCompanyId(carId , getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Car " + carId + " not found"));
        CarAttachment att = carAttachmentRepo.findByIdAndCarId(attachmentId, carId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment " + attachmentId + " not found for car " + carId));
        storageService.delete(att.getFileKey());
        car.getAttachments().remove(att);
        return CarMapper.toDto(car);
    }


    public Page<CarResponseDTO> getAllCars(CarSearchCriteria criteria,Pageable pageable) {
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
                .chassisNumber((criteria.chassisNumber()))
                .model(criteria.model())
                .build();

        Specification<Car> spec = new CarSpecification(enrichedCriteria);
        return carRepository.findAll(spec, page)
                .map(CarMapper::toDto);
    }

    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }
}
