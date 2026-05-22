package com.ahd.backend.carcontracts.car.controller;
import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.service.CarService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"${application.api.base-path}/car"})
public class CarController {
  private final CarService carService;
  
  @Generated
  public CarController(CarService carService) {
    this.carService = carService;
  }
  
  @PostMapping(consumes = {"multipart/form-data"})
  @PreAuthorize("hasAuthority('CREATE_CAR') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<CarResponseDTO> create(@ModelAttribute @Valid CarRequestDTO car, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
    CarResponseDTO res = this.carService.createCar(car, (files == null) ? List.of() : files);
    return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(res);
  }
  
  @PutMapping({"/{id}"})
  @PreAuthorize("hasAuthority('UPDATE_CAR') or hasRole('SUPER_ADMIN')")
  public CarResponseDTO update(@PathVariable Long id, @RequestBody @Valid UpdateCarRequestDTO body) {
    return this.carService.updateCar(id, body);
  }
  
  @DeleteMapping({"/{id}"})
  @PreAuthorize("hasAuthority('DELETE_CAR') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> softDelete(@PathVariable Long id) {
    this.carService.softDeleteCar(id);
    return ResponseEntity.noContent().build();
  }
  
  @PostMapping(path = {"/{carId}/attachments"}, consumes = {"multipart/form-data"})
  @PreAuthorize("hasAuthority('CREATE_CAR') or hasRole('SUPER_ADMIN')")
  public CarResponseDTO addAttachments(@PathVariable Long carId, @RequestPart("files") List<MultipartFile> files) {
    return this.carService.addAttachments(carId, files);
  }
  
  @DeleteMapping({"/{carId}/attachments/{attId}"})
  @PreAuthorize("hasAuthority('DELETE_CAR') or hasRole('SUPER_ADMIN')")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity deleteAttachment(@PathVariable Long carId, @PathVariable Long attId) {
    this.carService.deleteAttachment(carId, attId);
    return ResponseEntity.ok(ApiResponse.builder()
        .success(true)
        .message("Delete Attachment Successfully")
        .code(Integer.valueOf(200))
        .date(Instant.now())
        .build());
  }
  
  @GetMapping({"/{id}"})
  @PreAuthorize("hasAuthority('GET_CAR') or hasRole('SUPER_ADMIN')")
  public CarResponseDTO get(@PathVariable Long id) {
    return this.carService.getCar(id);
  }
  
  @GetMapping
  @PreAuthorize("hasAuthority('GET_CAR') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<?>> list(CarSearchCriteria criteria, @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
    Page<CarResponseDTO> cars = this.carService.getAllCars(criteria, pageable);
    return 
      ResponseEntity.status((HttpStatusCode)HttpStatus.OK)
      .body(ApiResponse.success(cars));
  }
}
