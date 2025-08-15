package com.ahd.backend.carcontracts.authorization.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationUpsertRequest {

    private Long authorizationNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate authorizationDate;
    private Long buyerId;
    private String companyAgent;
    private Long carId;
}
