package com.ahd.backend.carcontracts.car;

import com.ahd.backend.carcontracts.appuser.models.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.ahd.backend.carcontracts.S3.ImageStorageService;
import com.ahd.backend.carcontracts.S3.S3UrlService;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final ImageStorageService imageStorageService;
    private final S3UrlService s3UrlService;

    public Car createCar(Car car) {
        return carRepository.save(car);
    }

    public Car getCarById(Long id) {
        Car car = carRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));
        if (car.getImage() != null ) {
            car.setImage(s3UrlService.getImageUrl(car.getImage()));
        }
        return car ;
    }


    public Car updateCar(Long id, Car updatedCar) {
        Car car = carRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));

        car.setModel(updatedCar.getModel());
        car.setColor(updatedCar.getColor());
        car.setType(updatedCar.getType());
        car.setName(updatedCar.getName());
        car.setPlateNumber(updatedCar.getPlateNumber());
        car.setChassisNumber(updatedCar.getChassisNumber());
        car.setKilometers(updatedCar.getKilometers());
        car.setCylinderCount(updatedCar.getCylinderCount());
        car.setPassengerCount(updatedCar.getPassengerCount());
        car.setEngineType(updatedCar.getEngineType());
        car.setOrigin(updatedCar.getOrigin());

        return carRepository.save(car);
    }

    public void softDeleteCar(Long id) {
        Car car = carRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));

        car.setDeleted(true);
        carRepository.save(car);
    }

    @Transactional
    public Car updateCarPhoto(MultipartFile photo , Long Id) {
        Car car = carRepository.findById(Id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Car not found"));
        if (car.getImage() != null) {
            imageStorageService.delete(car.getImage());
        }
        String imageKey = imageStorageService.upload(photo);
        car.setImage(imageKey);
        carRepository.save(car);
        return car;
    }
}
