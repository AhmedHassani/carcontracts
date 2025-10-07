package com.ahd.backend.carcontracts.authorization.dto;

import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.person.model.Person;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthorizationResponse {
    private Long id;
    private Long companyId;
    private Long authorizationNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate authorizationDate;
    private String companyAgent;
    private PersonSummary buyer;
    private CarSummary car;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PersonSummary {
        private Long id;
        private String firstName;
        private String fatherName;
        private String grandfatherName;
        private String fourthName;
        private String surname;
        private String phoneNumber;
        private String nationalId;
        private String residenceCardNo;
        private String residence;
        private String district;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CarSummary {
        private Long id;
        private String name;
        private String type;
        private String color;
        private String model;
        private String plateNumber;
        private String chassisNumber;
        private Integer kilometers;
        private Integer cylinderCount;
        private Integer passengerCount;
        private String engineType;
        private String origin;
    }
}