package com.ahd.backend.carcontracts.authorization.mapper;


import com.ahd.backend.carcontracts.authorization.dto.AuthorizationResponse;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpsertRequest;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.person.model.Person;

public final class AuthorizationMapper {

    private AuthorizationMapper() { }

    // Entity -> Response DTO
    public static AuthorizationResponse toResponse(Authorization e) {
        if (e == null) return null;
        return AuthorizationResponse.builder()
                .id(e.getId())
                .authorizationNumber(e.getAuthorizationNumber())
                .authorizationDate(e.getAuthorizationDate())
                .companyAgent(e.getCompanyAgent())
                .buyer(toPersonSummary(e.getBuyer()))
                .car(toCarSummary(e.getCar()))
                .templateId(e.getTemplateId())
                .isChange(e.isChange())
                .build();
    }

    private static AuthorizationResponse.PersonSummary toPersonSummary(Person p) {
        if (p == null) return null;
        return AuthorizationResponse.PersonSummary.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .fatherName(p.getFatherName())
                .grandfatherName(p.getGrandfatherName())
                .fourthName(p.getFourthName())
                .surname(p.getSurname())
                .phoneNumber(p.getPhoneNumber())
                .nationalId(p.getNationalId())
                .residenceCardNo(p.getResidenceCardNo())
                .residence(p.getResidence())
                .district(p.getDistrict())
                .build();
    }

    private static AuthorizationResponse.CarSummary toCarSummary(Car c) {
        if (c == null) return null;
        return AuthorizationResponse.CarSummary.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .color(c.getColor())
                .model(c.getModel())
                .plateNumber(c.getPlateNumber())
                .chassisNumber(c.getChassisNumber())
                .kilometers(c.getKilometers())
                .cylinderCount(c.getCylinderCount())
                .passengerCount(c.getPassengerCount())
                .engineType(c.getEngineType())
                .origin(c.getOrigin())
                .build();
    }


    // Upsert DTO + resolved refs -> new Entity
    public static Authorization fromUpsertRequest(AuthorizationUpsertRequest r, Person buyer, Car car) {
        if (r == null) return null;
        return Authorization.builder()
                .authorizationNumber(r.getAuthorizationNumber())
                .authorizationDate(r.getAuthorizationDate())
                .buyer(buyer)
                .companyAgent(r.getCompanyAgent())
                .car(car)
                .build();
    }

    // Update existing Entity from Upsert DTO + resolved refs
    public static Authorization update(Authorization target, AuthorizationUpsertRequest r, Person buyer, Car car) {
        if (target == null || r == null) return target;
        target.setAuthorizationNumber(r.getAuthorizationNumber());
        target.setAuthorizationDate(r.getAuthorizationDate());
        target.setBuyer(buyer);
        target.setCompanyAgent(r.getCompanyAgent());
        target.setCar(car);
        return target;
    }
}
