package com.ahd.backend.carcontracts.car.service;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.mapper.CarMapper;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.model.CarAttachment;
import com.ahd.backend.carcontracts.car.repository.CarAttachmentRepository;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.DuplicateResourceException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Transactional
    public CarResponseDTO createCar(CarRequestDTO dto, List<MultipartFile> files) {
        if (carRepository.existsByChassisNumber(dto.getChassisNumber())) {
            throw new DuplicateResourceException(
                    "chassisNumber", dto.getChassisNumber(), "Car with this chassis number already exists");
        }
        if (carRepository.existsByPlateNumber(dto.getPlateNumber())) {
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
        Car car = carRepository.findWithAttachmentsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car " + id + " not found"));
        return CarMapper.toDto(car);
    }

    @Transactional
    public CarResponseDTO updateCar(Long id, UpdateCarRequestDTO patch) {
        if (patch == null || patch.isEmpty()) {
            throw new BadRequestException("Update payload must contain at least one field");
        }
        Car car = carRepository.findById(id)
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

    public void softDeleteCar(Long id) {
        Car car = carRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));
        car.setDeleted(true);
        carRepository.save(car);
    }

    @Transactional
    public CarResponseDTO addAttachments(Long carId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("No files provided");
        }
        Car car = carRepository.findById(carId)
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
    public CarResponseDTO deleteAttachment(Long carId, Long attachmentId) {
        Car car = carRepository.findById(carId)
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
        Specification<Car> spec = new CarSpecification(criteria);
        return carRepository.findAll(spec, page)
                .map(CarMapper::toDto);
    }

}
