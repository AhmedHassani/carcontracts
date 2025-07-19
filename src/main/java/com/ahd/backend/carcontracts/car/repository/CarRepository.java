package com.ahd.backend.carcontracts.car.repository;

import com.ahd.backend.carcontracts.car.model.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
    Optional<Car> findByIdAndDeletedFalse(Long id);
    Optional<Car> findById(Long id);
    boolean existsByChassisNumber(String chassisNumber);
    boolean existsByPlateNumber(String plateNumber);
    @EntityGraph(attributePaths = "attachments")
    Optional<Car> findWithAttachmentsById(Long id);
}
