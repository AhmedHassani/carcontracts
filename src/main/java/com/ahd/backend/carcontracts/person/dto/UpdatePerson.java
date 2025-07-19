package com.ahd.backend.carcontracts.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePerson {
    private long id;
    private String firstName;
    private String fatherName;
    private String grandfatherName;
    private String fourthName;
    private String surname;
    @Pattern(regexp = "^\\d{10,15}$",
            message = "Phone number must contain 10-15 digits")
    private String phoneNumber;
    private String nationalId;
    private String residenceCardNo;
    private String residence;
    private String district;
    private String alley;
    private String houseNo;
    private String issuingAuthority;
    private String infoOffice;
}
