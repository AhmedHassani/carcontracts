package com.ahd.backend.carcontracts.email;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Format {
    @NotNull(message = "toEmail is required")
    String toEmail;
    String cc;
    @NotNull(message = "ownerName is required")
    String ownerName;
    @NotNull(message = "companyUsername is required")
    String companyUsername;
    @NotNull(message = "companyPassword is required")
    String companyPassword;
}
