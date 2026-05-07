package com.ahd.backend.carcontracts.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Format {
    
    @NotNull(message = "toEmail is required")
    @Email(message = "Invalid email format")
    private String toEmail;
    
    private String[] cc;
    
    @NotNull(message = "ownerName is required")
    private String ownerName;
    
    @NotNull(message = "companyUsername is required")
    private String companyUsername;
    
    @NotNull(message = "companyPassword is required")
    private String companyPassword;
}