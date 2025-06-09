package com.ahd.backend.carcontracts.car;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public Car createCar(Car car) {
        return carRepository.save(car);
    }

    public Car getCarById(Long id) {
        return carRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Car not found or already deleted"));
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

}
