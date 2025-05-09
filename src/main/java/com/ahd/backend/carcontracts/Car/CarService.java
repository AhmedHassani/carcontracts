package com.ahd.backend.carcontracts.Car;

import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public Car createCar(Car car) {
        return carRepository.save(car);
    }

    public Car getCarById(Long id) {
        return carRepository.findByIdAndDeletedFalse(id);
    }

    public Car updateCar(Long id, Car updatedCar) {
        Car car = carRepository.findByIdAndDeletedFalse(id);

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
        car.setCompany(updatedCar.getCompany());

        return carRepository.save(car);
    }

    public void softDeleteCar(Long id) {
        Car car = carRepository.findByIdAndDeletedFalse(id);
        car.setDeleted(true);
        carRepository.save(car);
    }
}
