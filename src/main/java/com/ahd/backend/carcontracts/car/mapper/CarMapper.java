package com.ahd.backend.carcontracts.car.mapper;

import com.ahd.backend.carcontracts.car.dto.CarRequestDTO;
import com.ahd.backend.carcontracts.car.dto.CarResponseDTO;
import com.ahd.backend.carcontracts.car.dto.UpdateCarRequestDTO;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.model.CarAttachment;
import com.ahd.backend.carcontracts.contract.dto.ContractResponse;
import com.ahd.backend.carcontracts.person.model.Person;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CarMapper {
  
  public static Car toEntity(CarRequestDTO dto) {
    if (dto == null)
      return null; 

      String carPrice = dto.getCarPrice();
    if (carPrice == null || carPrice.isEmpty() || "null".equalsIgnoreCase(carPrice)) {
        carPrice = "0";
    }

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
      .carPrice(carPrice)
      .annualContractNumber(dto.getAnnualContractNumber())
      .annualContractDate(dto.getAnnualContractDate())  // ← Fixed: no parsing needed
      .inspectionDate(dto.getInspectionDate())          // ← Fixed: no parsing needed
      .build();
  }
  
  public static CarResponseDTO toDto(Car car) {
    if (car == null)
      return null; 

       
    List<CarAttachment> attachmentDTOs = car.getAttachments().stream()
      .map(com.ahd.backend.carcontracts.car.mapper.CarMapper::toDto)
      .collect(Collectors.toList());
    return CarResponseDTO.builder()
      .id(car.getId())
      .name(car.getName())
      .type(car.getType())
      .color(car.getColor())
      .paidAt(car.getPaidAt())
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
      .annualContractNumber(car.getAnnualContractNumber())
      .annualContractDate(car.getAnnualContractDate())
      .inspectionDate(car.getInspectionDate())
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
    Optional.ofNullable(patch.getTypeOfCarPlate()).ifPresent(target::setTypeOfCarPlate);
    Optional.ofNullable(patch.getInitPrice()).ifPresent(target::setInitPrice);
    Optional.ofNullable(patch.getDescription()).ifPresent(target::setDescription);
    // Don't handle carPrice here - it's handled in the service
    // Don't handle currentPossessorId here - it's handled in the service
    Optional.ofNullable(patch.getAnnualContractNumber()).ifPresent(target::setAnnualContractNumber);
    Optional.ofNullable(patch.getAnnualContractDate()).ifPresent(target::setAnnualContractDate);
    Optional.ofNullable(patch.getInspectionDate()).ifPresent(target::setInspectionDate);
    Optional.ofNullable(patch.getWalletNumber()).ifPresent(target::setWalletNumber);
    
    return target;
}
}