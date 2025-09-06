package com.ahd.backend.carcontracts.car.mapper;

import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.model.CarAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Utility-class, no state.
 */
public final class CarMapper {

    private CarMapper() {
    }

    public static Car toEntity(CarRequestDTO dto) {
        if (dto == null) return null;
        return Car.builder()
                .name(dto.getName())
                .type(dto.getType())
                .color(dto.getColor())
                .model(dto.getModel())
                .plateNumber(dto.getPlateNumber())
                .chassisNumber(dto.getChassisNumber())
                .kilometers(dto.getKilometers())
                .cylinderCount(dto.getCylinderCount())
                .passengerCount(dto.getPassengerCount())
                .engineType(dto.getEngineType())
                .companyId(dto.getCompnayId())
                .origin(dto.getOrigin())
                .attachments(new ArrayList<>())
                .build();
    }

    /* ─────────────── Entity ➜ DTO ─────────────── */

    public static CarResponseDTO toDto(Car car) {
        if (car == null) return null;

        List<CarAttachment> attachmentDTOs =
                car.getAttachments().stream()
                        .map(CarMapper::toDto)
                        .collect(Collectors.toList());

        return CarResponseDTO.builder()
                .id(car.getId())
                .name(car.getName())
                .type(car.getType())
                .color(car.getColor())
                .model(car.getModel())
                .plateNumber(car.getPlateNumber())
                .chassisNumber(car.getChassisNumber())
                .kilometers(car.getKilometers())
                .cylinderCount(car.getCylinderCount())
                .passengerCount(car.getPassengerCount())
                .engineType(car.getEngineType())
                .origin(car.getOrigin())
                .attachments(attachmentDTOs)
                .createdAt(car.getCreatedAt())
                .build();
    }

    /**
     * Single attachment ➜ DTO helper
     */
    private static CarAttachment toDto(CarAttachment att) {
        return CarAttachment.builder()
                .id(att.getId())
                .fileKey(att.getFileKey())
                .mimeType(att.getMimeType())
                .build();
    }

    /* ─────────────── PATCH helper ─────────────── */

    public static Car updateEntity(Car target, UpdateCarRequestDTO patch) {
        if (target == null || patch == null) return null;
        Optional.ofNullable(patch.getName()).ifPresent(target::setName);
        Optional.ofNullable(patch.getType()).ifPresent(target::setType);
        Optional.ofNullable(patch.getColor()).ifPresent(target::setColor);
        Optional.ofNullable(patch.getModel()).ifPresent(target::setModel);
        Optional.ofNullable(patch.getPlateNumber()).ifPresent(target::setPlateNumber);
        Optional.ofNullable(patch.getChassisNumber()).ifPresent(target::setChassisNumber);
        Optional.ofNullable(patch.getKilometers()).ifPresent(target::setKilometers);
        Optional.ofNullable(patch.getCylinderCount()).ifPresent(target::setCylinderCount);
        Optional.ofNullable(patch.getPassengerCount()).ifPresent(target::setPassengerCount);
        Optional.ofNullable(patch.getEngineType()).ifPresent(target::setEngineType);
        Optional.ofNullable(patch.getOrigin()).ifPresent(target::setOrigin);
        return target;
    }
}