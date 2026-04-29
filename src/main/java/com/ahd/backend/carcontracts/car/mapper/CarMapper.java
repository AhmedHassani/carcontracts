package com.ahd.backend.carcontracts.car.mapper;

import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.model.CarAttachment;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.person.model.Person;
// import com.ahd.backend.carcontracts.person.ma;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CarMapper {
  public static Car toEntity(CarRequestDTO dto) {
    if (dto == null)
      return null; 
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
      .attachments(new ArrayList())
      .walletNumber(dto.getWalletNumber())
      .typeOfCarPlate(dto.getTypeOfCarPlate())
      .initPrice(dto.getInitPrice())
      .description(dto.getDescription())
      .carPrice(dto.getCarPrice())
      .build();
  }
  
  public static CarResponseDTO toDto(Car car) {
    if (car == null)
      return null; 
    List<CarAttachment> attachmentDTOs = (List<CarAttachment>)car.getAttachments().stream().map(com.ahd.backend.carcontracts.car.mapper.CarMapper::toDto).collect(Collectors.toList());
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
      .walletNumber(car.getWalletNumber())
      .typeOfCarPlate(car.getTypeOfCarPlate())
      .status(car.getStatus())
      .initPrice(car.getInitPrice())
      .description(car.getDescription())
      .currentPossessor(toPersonDTO(car.getCurrentPossessor()))
      .carPrice(car.getCarPrice())
      .build();
  }
  
  private static ContractResponse.PersonDTO toPersonDTO(Person p) {
    if (p == null)
      return null; 
    ContractResponse.PersonDTO dto = new ContractResponse.PersonDTO();
    dto.setId(p.getId());
    dto.setCompanyId(p.getCompanyId());
    dto.setFirstName(p.getFirstName());
    dto.setFatherName(p.getFatherName());
    dto.setGrandfatherName(p.getGrandfatherName());
    dto.setFourthName(p.getFourthName());
    dto.setSurname(p.getSurname());
    dto.setFullName(p.getFirstName() + " " + p.getFirstName() + " " + p.getFatherName());
    dto.setPhone(p.getPhoneNumber());
    dto.setNationalId(p.getNationalId());
    dto.setResidenceCardNo(p.getResidenceCardNo());
    dto.setResidence(p.getResidence());
    dto.setDistrict(p.getDistrict());
    dto.setAlley(p.getAlley());
    dto.setHouseNo(p.getHouseNo());
    dto.setIssuingAuthority(p.getIssuingAuthority());
    dto.setInfoOffice(p.getInfoOffice());
    return dto;
  }
  
  private static CarAttachment toDto(CarAttachment att) {
    return CarAttachment.builder()
      .id(att.getId())
      .fileKey(att.getFileKey())
      .mimeType(att.getMimeType())
      .build();
  }
  
  public static Car updateEntity(Car target, UpdateCarRequestDTO patch) {
    if (target == null || patch == null)
      return null; 
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getName()).ifPresent(target::setName);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getType()).ifPresent(target::setType);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getColor()).ifPresent(target::setColor);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getModel()).ifPresent(target::setModel);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getPlateNumber()).ifPresent(target::setPlateNumber);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getChassisNumber()).ifPresent(target::setChassisNumber);
    Objects.requireNonNull(target);
    Optional.<Integer>ofNullable(patch.getKilometers()).ifPresent(target::setKilometers);
    Objects.requireNonNull(target);
    Optional.<Integer>ofNullable(patch.getCylinderCount()).ifPresent(target::setCylinderCount);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getPassengerCount()).ifPresent(target::setPassengerCount);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getEngineType()).ifPresent(target::setEngineType);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getOrigin()).ifPresent(target::setOrigin);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getTypeOfCarPlate()).ifPresent(target::setTypeOfCarPlate);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getInitPrice()).ifPresent(target::setInitPrice);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getDescription()).ifPresent(target::setDescription);
    Objects.requireNonNull(target);
    Optional.<String>ofNullable(patch.getCarPrice()).ifPresent(target::setCarPrice);
    return target;
  }
}
