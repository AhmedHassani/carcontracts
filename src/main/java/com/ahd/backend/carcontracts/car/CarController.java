package com.ahd.backend.carcontracts.car;

import com.ahd.backend.carcontracts.appuser.models.UserDetailsDTO;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/carcontracts/v1/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping
    public ResponseEntity<Car> create(@RequestBody Car car) {
        return ResponseEntity.ok(carService.createCar(car));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Car> update(@PathVariable Long id, @RequestBody Car car) {
        return ResponseEntity.ok(carService.updateCar(id, car));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        carService.softDeleteCar(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Car> updateCarPhoto(
            @PathVariable Long id,
            @RequestPart("photo") MultipartFile photo) {

        Car updatedCar = carService.updateCarPhoto(photo, id);

        return ResponseEntity.ok(updatedCar);
    }

}
