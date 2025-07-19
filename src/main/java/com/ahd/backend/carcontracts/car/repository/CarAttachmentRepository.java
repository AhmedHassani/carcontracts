package com.ahd.backend.carcontracts.car.repository;

import com.ahd.backend.carcontracts.car.model.CarAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface CarAttachmentRepository extends JpaRepository<CarAttachment, Long> {
    Optional<CarAttachment> findByIdAndCarId(Long id, Long carId);

}