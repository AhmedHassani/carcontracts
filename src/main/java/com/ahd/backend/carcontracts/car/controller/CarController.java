package com.ahd.backend.carcontracts.car.controller;

import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.service.CarService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import com.ahd.backend.carcontracts.util.base.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarResponseDTO> create(@ModelAttribute @Valid CarRequestDTO car, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        CarResponseDTO res = carService.createCar(car, files == null ? List.of() : files);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }


    @PutMapping("/{id}")
    public CarResponseDTO update(@PathVariable Long id,
                                 @RequestBody @Valid UpdateCarRequestDTO body) {
        return carService.updateCar(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        carService.softDeleteCar(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping(path = "/{carId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CarResponseDTO addAttachments(@PathVariable Long carId,
                                         @RequestPart("files") List<MultipartFile> files) {
        return carService.addAttachments(carId, files);
    }

    @DeleteMapping("/{carId}/attachments/{attId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity deleteAttachment(@PathVariable Long carId,
                                 @PathVariable Long attId) {
        carService.deleteAttachment(carId, attId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Delete Attachment Successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    public CarResponseDTO get(@PathVariable Long id) {
        return carService.getCar(id);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(CarSearchCriteria criteria,
                                 @PageableDefault(sort = "id", direction = Sort.Direction.ASC)
                                 Pageable pageable) {
        Page<CarResponseDTO> cars = carService.getAllCars(criteria, pageable);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(cars));
    }

}
