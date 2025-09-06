package com.ahd.backend.carcontracts.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequestDTO {
    @NotBlank
    private String firstName;
    @NotBlank
    private String fatherName;
    @NotBlank
    private String grandfatherName;
    @NotBlank
    private String fourthName;
    @NotBlank
    private String surname;
    @NotBlank
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
    private Long companyId;
    @NotNull(message = "NATIONAL_ID_FRONT file is required")
    private MultipartFile nationalIdFrontFile;

    @NotNull(message = "NATIONAL_ID_BACK file is required")
    private MultipartFile nationalIdBackFile;

    @NotNull(message = "RESIDENCE_CARD_FRONT file is required")
    private MultipartFile residenceCardFrontFile;

    @NotNull(message = "RESIDENCE_CARD_BACK file is required")
    private MultipartFile residenceCardBackFile;
    private MultipartFile [] othreFiles;

}
